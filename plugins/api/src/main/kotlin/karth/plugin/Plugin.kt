package karth.plugin

import com.github.michaelbull.logging.InlineLogger
import gearth.extensions.ExtensionForm
import gearth.protocol.HMessage
import gearth.protocol.HPacket
import io.netty.channel.Channel
import karth.core.KarthSession
import karth.core.message.Message
import karth.core.protocol.Packet
import karth.core.protocol.PacketStructureCodecFactory
import tornadofx.booleanProperty
import tornadofx.onChange
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.reflect.full.findAnnotation
import kotlin.time.Duration

/**
 * Represents a Karth enabled wrapper around G-Earth [extensions][ExtensionForm].
 *
 * @param C the type of the [JavaFX Controller][controller] for this plugin.
 */
abstract class Plugin<C : Any> : ExtensionForm() {

    /**
     * [logger instance][InlineLogger] for this plugin.
     */
    private val logger by lazy { InlineLogger(getInfo().let {"${it.title}:${it.version}"}) }

    /**
     * Used to schedule [sending][send] of [messages][Message].
     *
     * @see sendWithDelay usage of scheduler for delayed sending of messages.
     */
    private val scheduler by lazy { Timer() }

    /**
     * Contains a queue of pending outgoing messages.
     */
    private val pendingMessages by lazy { ConcurrentLinkedDeque<ExtensionMessage.Outgoing.SendMessage>() }

    /**
     * Backing boolean property of [useQueue], used to validate [pendingMessages] is empty
     * before the value of [useQueue] is changed.
     */
    private val useQueueProperty by lazy {
        booleanProperty(false).apply {
            onChange {
                if (!it) {
                    if (pendingMessages.isNotEmpty()) {
                        logger.warn { "`useQueue` was set to false but queue is not empty, will discard ${pendingMessages.size} messages!" }
                        clearPendingMessages()
                    }
                }
            }
        }
    }

    /**
     * When `true` outgoing messages are added to [pendingMessages] instead of written directly to [channel].
     *
     * This is done to prevent outgoing messages to be written before the intercepted message that caused them.
     *
     * Below follows a rough communication overview between the Habbo client,
     * G-Earth, extensions, and the Habbo server:
     *
     *  1. Habbo client sends message to Habbo server
     *  2. g-earth intercepts message
     *  3. g-earth sends intercepted message to extension
     *  4. extension executes logic listening for intercepted message
     *  5. extension queues outgoing message(s) created in aforementioned logic
     *  6. extension sends manipulated message back to g-earth
     *  7. extension sends queued message(s)
     *  8. g-earth receives manipulated packet
     *  9. g-earth receives queued message(s)
     * 10. g-earth sends message(s) to Habbo server
     *
     * If instead of queuing the outgoing messages in step 5, the extension would write
     * them directly to the [channel], then these messages would be written
     * before the originally intercepted message in step 2.
     *
     * This causes abnormal behaviour because the Habbo server will receive the response
     * to a packet before it receives the message it is responding to.
     */
    var useQueue: Boolean
        get() = synchronized(this, useQueueProperty::get)
        set(value) {  synchronized(this) { useQueueProperty.set(value) } }


    /**
     * Represents a FXML controller which should be specified in the [PluginInfo.fxmlFile].
     */
    lateinit var controller: C

    /**
     * A [KarthSession] instance, handles both the encoding/decoding of [packets][HPacket] to [messages][Message],
     * and the listeners/handler for/of [messages][Message].
     */
    lateinit var session: KarthSession

    /**
     * The [Channel] facilitating the connection between this plugin and an instance of G-Earth.
     */
    lateinit var channel: Channel

    /**
     * The codec specifies the packet structure for [messages][Message].
     */
    abstract val codecs: PacketStructureCodecFactory

    /**
     * Sets [session] as a new [KarthSession] instance.
     */
    public final override fun initExtension() {
        session = KarthSession(this, codecs)
    }

    /**
     * Encodes the [message] as a [HPacket] and writes the messages to the [channel].
     *
     * This can be used to send both [Message.Incoming] and [Message.Outgoing] packets.
     *
     * @see [KarthSession.send] the method this method is a delegate for.
     */
    fun <O : Packet> send(message: O) =
        session.send(message)

    /**
     * Sends the [message] after the specified [delay].
     */
    fun < O : Packet> sendWithDelay(message: O, delay: Duration) =
        scheduler.schedule(object : TimerTask(){
            override fun run() {
                send(message)
            }
        }, delay.inWholeMilliseconds)

    inline fun <reified I : Message> onEach(
        crossinline onException: (Message.HandleException) -> Unit = {},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> Unit,
    ) = session.onEach(onException, condition, handler)

    override fun sendToClient(packet: HPacket) = send(packet, HMessage.Direction.TOCLIENT)

    override fun sendToServer(packet: HPacket) = send(packet, HMessage.Direction.TOSERVER)

    private fun send(packet: HPacket, direction: HMessage.Direction): Boolean {

        if (packet.isCorrupted)
            return false

        if (!packet.isPacketComplete)
            packet.completePacket(packetInfoManager)

        if (!packet.isPacketComplete)
            return false

        if (!channel.isOpen)
            return false

        val outgoingMessage = ExtensionMessage.Outgoing.SendMessage(packet, direction)
        if (useQueue)
            pendingMessages.add(outgoingMessage)
        else
            channel.writeAndFlush(outgoingMessage)
        return true
    }

    /**
     * Polls each [pendingMessages] and writes it to the [channel], then flushes the [channel].
     */
    fun writeAndFLushPendingMessages() {
        while(pendingMessages.isNotEmpty())
            channel.write(pendingMessages.poll())
        channel.flush()
    }

    /**
     * Clears all [pendingMessages].
     */
    fun clearPendingMessages() = pendingMessages.clear()

    /**
     * Gets the [PluginInfo] annotation instance of this plugin.
     */
    fun getInfo() = this::class.findAnnotation<PluginInfo>()
        ?: throw Exception("Missing PluginInfo annotation")
}
