package dorving.karth

import dorving.karth.junit.KarthTestingExtension
import dorving.karth.junit.TestMock
import dorving.karth.message.Message.*
import dorving.karth.message.encodeAsHMessage
import gearth.extensions.Extension
import gearth.protocol.HMessage.Direction.TOCLIENT
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@ExtendWith(KarthTestingExtension::class)
class KarthSessionTest {

    @TestMock
    lateinit var session: KarthSession

    @TestMock
    lateinit var extension: Extension

    @Test
    fun `chat message`(){

        val message = Incoming.Chat(69, "test")


        val lock = CountDownLatch(1)
        // listen for incoming messages of type Chat
        session.onEach<Incoming.Chat> {
            assertEquals(message, this)
            lock.countDown()
        }
        extension.modifyMessage(message.encodeAsHMessage(TOCLIENT))

        assertTrue(lock.await(1L, TimeUnit.SECONDS))
    }
}
