package karth.core

import com.github.michaelbull.logging.InlineLogger
import gearth.extensions.IExtension
import gearth.protocol.HMessage
import gearth.protocol.HMessage.Direction.TOCLIENT
import gearth.protocol.HMessage.Direction.TOSERVER
import gearth.protocol.HPacket
import gearth.protocol.connection.HClient
import karth.core.message.Message
import karth.core.message.MessageListener
import karth.core.protocol.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class KarthSession(val extension: IExtension, private val factory: PacketStructureCodecFactory) {

    val logger = InlineLogger(KarthSession::class)

    lateinit var client: HClient
    lateinit var codec: PacketStructureCodec

    private val listeners = Collections.synchronizedList<MessageListener>(mutableListOf())

    fun connect(hClient: HClient) {
        logger.debug { "Connected to G-Earth (client=$hClient)" }
        client = hClient
        codec = factory.getCodec(hClient)
        logger.debug { "Loaded packet structure codec" }
        interceptMessages(TOCLIENT)
        interceptMessages(TOSERVER)
    }

    private fun interceptMessages(direction: HMessage.Direction) {
        extension.intercept(direction) { hMessage ->

            val packetId = hMessage.packet.headerId()

            val packetInfo = extension.packetInfoManager
                .getPacketInfoFromHeaderId(direction, packetId)
            if (packetInfo == null) {
                logger.error { "PacketInfo for packet not defined (packetId=$packetId, packet=$hMessage)" }
                return@intercept
            }

            val packetName = packetInfo.name

            val structure = when (direction) {
                TOSERVER -> codec.server.forName(packetName)
                TOCLIENT -> codec.client[packetName]
            }
            if (structure == null) {
                logger.error { "Structure for packet not defined (name=$packetName)" }
                return@intercept
            }

            val reader = structure.read
            if (reader == null) {
                logger.error { "PacketReader for packet not defined (name=$packetName)" }
                return@intercept
            }

            val packet = hMessage.packet
            packet.resetReadIndex()
            try {
                val message = reader.invoke(packet, packetInfo) as Message
                message.hMessage = hMessage
                message.packetInfo = packetInfo
                listeners.removeIf { it.onMessage(message) == MessageListener.Response.REMOVE }
            } catch (e: Exception) {
                logger.error(e) { "Failed to read packet (packet=$packet)" }
            }
        }
    }
    fun addListener(listener: MessageListener): MessageListener {
        println("adding listener")
        listeners += listener
        println("added listener")
        return listener
    }

    fun removeListener(listener: MessageListener) {
        listeners -= listener
    }

    inline fun <reified I : Message> onEach(
        crossinline onException: (Message.HandleException) -> Unit = {logger.error(it){}},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> Unit,
    ): MessageListener = onEachMap(onException, condition, handler)

    inline fun <reified I : Message, R> onEachMap(
        crossinline onException: (Message.HandleException) -> Unit = {logger.error(it){}},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> R,
    ): MessageListener = addListener { message ->
        if (message is I) {
            try {
                if (condition(message))
                    handler(message)
            } catch (e: Exception) {
                onException(Message.HandleException(message, e))
            }
        }
        return@addListener MessageListener.Response.CONTINUE
    }

    fun <O : Packet> send(message: O) {
        logger.info { "Sending $message" }
        val packet = message.encode() ?: return
        val send = when (message) {
            is ClientPacket -> extension.sendToClient(packet)
            is ServerPacket ->extension.sendToServer(packet)
            else -> error("Invalid packet type (=$this)")
        }
        if (!send)
            logger.error { "Failed to send packet (packet=$packet)" }
    }

     fun <O : Packet> O.encode(): HPacket? {

        val (destination, structure) = when (this) {
            is ClientPacket -> TOCLIENT to codec.client.structures.values.find { it.clazz.isInstance(this) }as? PacketStructure<O>
            is ServerPacket -> TOSERVER to codec.server.structures[this::class] as? PacketStructure<O>
            else -> error("Invalid packet type (=$this)")
        }

        if (structure == null) {
            logger.error { "Structure for packet not defined (packet=${this::class})" }
            return null
        }
        val packetInfo = extension.packetInfoManager.let {
            it.getPacketInfoFromHeaderId(destination, structure.headerId)
                ?: it.getPacketInfoFromName(destination, structure.name)
        }
        if (packetInfo == null) {
            logger.error { "PacketInfo for structure not define (packet=$structure)" }
            return null
        }
        val packet = HPacket(packetInfo.name, packetInfo.destination)
        structure.write(this, packet)
        packet.overrideEditedField(false)
        return packet
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified O : ServerPacket, reified I : ClientPacket> sendAndReceive(
        toSend: O,
        maxAttempts: Int = 10,
        maxWaitTime: Duration = 1000.milliseconds,
        crossinline onException: (Exception) -> Unit = {logger.error(it){}},
        crossinline condition: I.() -> Boolean = { true },
    ) = sendAndExpectMap(toSend, maxAttempts, maxWaitTime, onException, condition) { this }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified O : ServerPacket, reified I : ClientPacket, R> sendAndExpectMap(
        outMessage: O,
        maxAttempts: Int = 10,
        maxWaitTime: Duration = 1000.milliseconds,
        crossinline onException: (Exception) -> Unit = {logger.error(it){}},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> R,
    ): R? {
        var result: R? = null
        val latch = CountDownLatch(1)
        val temporaryMessageListener = onEachMap(onException, condition) {
            result = handler(this)
            latch.countDown()
        }
        try {
            val outPacket = outMessage.encode()
            var attempt = maxAttempts
            while (attempt-- > 0) {
                extension.sendToServer(outPacket)
                if (latch.await(maxWaitTime.inWholeMilliseconds, TimeUnit.MILLISECONDS))
                    break
            }
        } catch (e: Exception) {
            onException(e)
        } finally {
            removeListener(temporaryMessageListener)
        }
        return result
    }
}
