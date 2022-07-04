package dorving.karth

import dorving.karth.message.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MessageTest {

    @Test
    fun `Resolve Message Codec`(){
        val message = Message.Incoming.Chat(0, "69")
        assertEquals(MessageEncoder.IncomingChatEncoder, message.findEncoder(), "Did not find encoder for Chat message")
        assertEquals(MessageDecoder.IncomingChatDecoder, message.findDecoder(), "Did not find decoder for Char message")


    }
}
