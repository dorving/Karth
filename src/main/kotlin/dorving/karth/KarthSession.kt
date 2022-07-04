package dorving.karth

import dorving.karth.message.*
import gearth.extensions.IExtension
import gearth.protocol.HMessage
import gearth.protocol.HMessage.Direction.TOCLIENT
import gearth.protocol.HMessage.Direction.TOSERVER
import gearth.protocol.HPacket
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

open class KarthSession(val extension: IExtension) {

    private val listeners = Collections.synchronizedList<MessageListener>(mutableListOf())

    init {
        extension.intercept(TOSERVER, ::onMessage)
        extension.intercept(TOCLIENT, ::onMessage)
    }

    fun addListener(listener: MessageListener): MessageListener {
        listeners += listener
        return listener
    }

    fun removeListener(listener: MessageListener) {
        listeners -= listener
    }

    inline fun <reified I : Message> onEach(
        crossinline onException: (Message.HandleException) -> Unit = {},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> Unit,
    ) = onEachMap(onException, condition, handler)

    inline fun <reified I : Message, R> onEachMap(
        crossinline onException: (Message.HandleException) -> Unit = {},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> R,
    ): MessageListener {
        return addListener { message ->
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
    }

    inline fun <reified O : Message> send(outMessage: O) {
        val encoder: MessageEncoder<O> = outMessage.findEncoder()
            ?: throw MessageEncoder.NotFoundException(outMessage)
        val packet: HPacket = encoder.encode(outMessage)
        extension.sendToServer(packet)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified O : Message, reified I : Message> sendAndReceive(
        toSend: O,
        maxAttempts: Int = 10,
        maxWaitTime: Duration = 1000.milliseconds,
        crossinline onException: (Exception) -> Unit = {},
        crossinline condition: I.() -> Boolean = { true },
        crossinline onReceive: I.() -> Unit
    ) = sendAndExpectMap(toSend, maxAttempts, maxWaitTime, onException, condition, onReceive)

    @Suppress("UNCHECKED_CAST")
    inline fun <reified O : Message, reified I : Message, R> sendAndExpectMap(
        outMessage: O,
        maxAttempts: Int = 10,
        maxWaitTime: Duration = 1000.milliseconds,
        crossinline onException: (Exception) -> Unit = {},
        crossinline condition: I.() -> Boolean = { true },
        crossinline handler: I.() -> R,
    ) : R? {
        val encoder: MessageEncoder<O>
        try {
            encoder = outMessage.findEncoder() ?: throw MessageEncoder.NotFoundException(outMessage)
        } catch (e: Exception) {
            onException(e)
            return null
        }

        var result: R? = null
        val latch = CountDownLatch(1)
        val temporaryMessageListener = onEachMap(onException, condition) {
            result = handler(this)
            latch.countDown()
        }
        try {
            val outPacket: HPacket = encoder.encode(outMessage)
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

    private fun onMessage(hMessage: HMessage) {
        val packetInfo = hMessage.getPacketInfo() ?: return
        val inMessageClass = MessageCodec.getMessageClass(hMessage, packetInfo)
        if (inMessageClass != null) {
            val message = decodeMessage<Message>(inMessageClass, hMessage)
            listeners.removeIf {
                it.onMessage(message) == MessageListener.Response.REMOVE
            }
        }
    }

    private inline fun <reified I : Message> decodeMessage(
        inMessageClass: KClass<out Message>,
        hMessage: HMessage,
    ): I {
        val inMessageDecoder: MessageDecoder<out Message> = inMessageClass.findDecoder()
            ?: throw MessageDecoder.NotFoundException(inMessageClass)
        val inPacket: HPacket = hMessage.packet
        val inMessage: I
        try {
            inPacket.resetReadIndex()
            inMessage = inMessageDecoder.decode(inPacket) as I
        } catch (e: Exception) {
            throw MessageDecoder.DecodeException(inPacket, inMessageClass, e)
        }
        return inMessage
    }

    private fun HMessage.getPacketInfo() =
        extension.packetInfoManager.getPacketInfoFromHeaderId(destination, packet.headerId())

}
