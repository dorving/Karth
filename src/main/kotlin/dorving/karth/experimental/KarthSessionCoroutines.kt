package dorving.karth.experimental

import dorving.karth.KarthSession
import dorving.karth.message.Message
import dorving.karth.message.MessageEncoder
import dorving.karth.message.MessageListener.Response.CONTINUE
import dorving.karth.message.findEncoder
import gearth.extensions.IExtension
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Deprecated("DO NOT USE, THIS DOES NOT YET WORK")
class KarthSessionCoroutines(extension: IExtension) : KarthSession(extension) {

    val listeningChannels = mutableMapOf<KClass<out Message>, MutableList<SendChannel<Message>>>()
    val incomingMessageDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    val outgoingMessageDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    companion object {
        val logger = Logger.getLogger("KarthSessionCoroutines")
    }

    init {
        addListener { message ->
            runBlocking(incomingMessageDispatcher) {
                listeningChannels[message::class]
                    ?.asFlow()
                    ?.onEach {
                        launch {
                            println("Sending $message to $it")
                            it.send(message)
                        }
                    }
                    ?.catch {
                        logger.log(Level.SEVERE, "Failed to send $message to channel", it)
                    }
                    ?.onCompletion { throwable ->
                        if (throwable != null)
                            logger.log(Level.SEVERE, "Encountered exception in flow receiving $message", throwable)
                        else
                            logger.log(Level.INFO, "Completed flow receiving $message")
                    }
                    ?.launchIn(this)
            }
            return@addListener CONTINUE
        }
    }

    suspend inline fun <reified O : Message, reified I : Message, R> sendAndExpectOne(
        outMessage: O,
        maxAttempts: Int = 10,
        maxWaitTime: Duration = 1000.milliseconds,
        capacity: Int = Channel.RENDEZVOUS,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
        crossinline onUndeliveredElement: ((I) -> Unit) = {},
    ) {
        val encoder: MessageEncoder<O> = outMessage.findEncoder()
            ?: throw MessageEncoder.NotFoundException(outMessage)

        val channel = listenFor(capacity, onBufferOverflow, onUndeliveredElement)
        try {
            val flow = channel.receiveAsFlow().filterIsInstance<I>()
            var attempt = maxAttempts
            while (attempt-- > 0) {
                withContext(outgoingMessageDispatcher) {
                    extension.sendToServer(encoder.encode(outMessage))
                }
                withTimeout(maxWaitTime) {
                    flow.collect()
                }
            }
        } finally {
            removeMessageListenerChannel<I>(channel)
        }
    }

    inline fun <reified M : Message> on(crossinline handler: M.() -> Unit) = runBlocking(incomingMessageDispatcher) {
        val channel = listenFor<M>()
        try {
            handler(channel.receiveCatching().getOrThrow() as M)
        } finally {
            removeMessageListenerChannel<M>(channel)
        }
    }

    suspend inline fun <reified M : Message> listenFor(
        capacity: Int = Channel.RENDEZVOUS,
        onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
        crossinline onUndeliveredElement: ((M) -> Unit) = {},
    ): Channel<Message> {
        val channel = Channel<Message>(capacity, onBufferOverflow, onUndeliveredElement = {
            onUndeliveredElement(it as M)
        })
        withContext(incomingMessageDispatcher) {
            listeningChannels.getOrPut(M::class) { mutableListOf() }.add(channel)
        }
        return channel
    }

    suspend inline fun <reified M : Message> removeMessageListenerChannel(channel: Channel<Message>) {
        withContext(incomingMessageDispatcher) {
            listeningChannels[M::class]!!.remove(channel)
        }
    }
}
