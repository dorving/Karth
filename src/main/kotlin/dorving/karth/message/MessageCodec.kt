package dorving.karth.message

import dorving.karth.message.Message.Header
import gearth.protocol.HMessage
import gearth.services.packet_info.PacketInfo
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

object MessageCodec {

    private val incomingMessages = Message.Incoming::class.nestedSubClasses()
    private val outgoingMessages = Message.Outgoing::class.nestedSubClasses()

    val messages: Map<KClass<out Message>, String> = (incomingMessages + outgoingMessages)
        .associateWith { it.findAnnotation<Header>()!!.name }

    val encoders: Map<KClass<out Message>, MessageEncoder<*>?> = messages.keys
        .associateWith {
            it.findAnnotation<Message.Encoder>()
                ?.encoderClass
                ?.objectInstance
        }

    val decoders: Map<KClass<out Message>, MessageDecoder<*>?> = messages.keys
        .associateWith {
            it.findAnnotation<Message.Decoder>()
                ?.decoderClass
                ?.objectInstance
        }

    private fun <O : Any> KClass<O>.nestedSubClasses() =
        nestedClasses
            .filter { it.isFinal && it.isSubclassOf(this) }
            .map { it as KClass<out O> }


    fun getMessageClass(hMessage: HMessage, info: PacketInfo): KClass<out Message>? {
        val superClass = when (hMessage.destination!!) {
            HMessage.Direction.TOSERVER -> Message.Outgoing::class
            HMessage.Direction.TOCLIENT -> Message.Incoming::class
        }
        return messages.entries
            .find { (type, name) -> info.name == name && type.isSubclassOf(superClass) }
            ?.key

    }
}

