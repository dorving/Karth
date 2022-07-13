package karth.plugin

import gearth.extensions.ExtensionForm
import gearth.protocol.HMessage
import gearth.protocol.HPacket
import io.netty.channel.Channel
import karth.core.KarthSession
import karth.core.message.Message
import karth.core.protocol.PacketStructureCodecFactory
import karth.core.protocol.ServerPacket
import java.util.*
import kotlin.reflect.full.findAnnotation
import kotlin.time.Duration

abstract class Plugin<C : Any> : ExtensionForm() {

    abstract val codecs: PacketStructureCodecFactory

    lateinit var controller: C

    lateinit var session: KarthSession
    lateinit var channel: Channel

    private val scheduler by lazy { Timer() }

    public final override fun initExtension() {
        session = KarthSession(this, codecs)
    }

    override fun sendToClient(packet: HPacket) =
        send(packet, HMessage.Direction.TOCLIENT)

    override fun sendToServer(packet: HPacket) =
        send(packet, HMessage.Direction.TOSERVER)

    private fun send(packet: HPacket, direction: HMessage.Direction): Boolean {

        if (packet.isCorrupted)
            return false

        if (!packet.isPacketComplete)
            packet.completePacket(packetInfoManager)

        if (!packet.isPacketComplete)
            return false

        if (!channel.isOpen)
            return false

        channel.writeAndFlush(ExtensionMessage.Outgoing.SendMessage(packet, direction))
        return true
    }

    fun <O : ServerPacket> send(outMessage: O) =
        session.send(outMessage)

    fun < O : ServerPacket> sendWithDelay(outMessage: O, delayBy: Duration) =
        scheduler.schedule(object : TimerTask(){
            override fun run() {
                send(outMessage)
            }
        }, delayBy.inWholeMilliseconds)

    inline fun <reified I : Message> onEach(
        crossinline onException: (Message.HandleException) -> Unit = {},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> Unit,
    ) = session.onEach(onException, condition, handler)

    fun getInfo() = this::class.findAnnotation<PluginInfo>()
        ?: throw Exception("Missing PluginInfo annotation")
}
