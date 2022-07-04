package dorving.karth.message

import gearth.protocol.HMessage
import gearth.protocol.HMessage.Direction
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
fun <O : Message> KClass<out O>.findDecoder() = MessageCodec.decoders[this] as? MessageDecoder<O>

@Suppress("UNCHECKED_CAST")
fun <O : Message> O.findDecoder() = MessageCodec.decoders[this::class] as? MessageDecoder<O>

@Suppress("UNCHECKED_CAST")
fun <O : Message> O.findEncoder() = MessageCodec.encoders[this::class] as? MessageEncoder<O>

fun <O : Message> O.encode() = findEncoder()?.encode(this)

fun <O : Message> O.encodeAsHMessage(direction: Direction, index: Int = 0) =
    HMessage(findEncoder()?.encode(this), direction, index)
