package karth.protocol.habbo

import gearth.protocol.HPacket
import gearth.protocol.connection.HClient
import gearth.services.packet_info.PacketInfo
import karth.core.protocol.HEADER_ID_UNDEFINED
import karth.core.protocol.Packet
import karth.core.protocol.PacketStructureCodecFactory
import kotlin.reflect.full.createInstance


private typealias SmartWriter<T> = T.(HPacket) -> Unit
internal typealias SmartReader<T> = HPacket.(PacketInfo) -> T


fun PacketStructureCodecFactory.codec(init: CodecBuilder.() -> Unit) {
    CodecBuilder().apply(init)
}


class CodecBuilder {

    inline fun <reified T : Packet> register(
        init: Builder<T>.(HClient) -> Unit = { defaultInit() }
    ) = register(emptyArray(), init)

    inline fun <reified T : Packet> register(
        onlyFor: HClient,
        init: Builder<T>.(HClient) -> Unit = { defaultInit() }
    ) = register(arrayOf(onlyFor), init)

    inline fun <reified T : Packet> register(
        onlyFor: Array<HClient> = emptyArray(),
        init: Builder<T>.(HClient) -> Unit = { defaultInit() }
    ) {

    }

    inline fun <reified T : Packet> Builder<T>.defaultInit() {
        read {
            T::class.objectInstance ?: T::class.createInstance()
        }
        write {}
    }
}

class Builder<T : Packet> {

    private lateinit var reader: SmartReader<T>
    private lateinit var writer: SmartWriter<T>

    lateinit var name: String
    var headerId: Int = HEADER_ID_UNDEFINED


    fun read(smartReader: SmartReader<T>) {
        reader = smartReader
    }


    fun write(smartWriter: SmartWriter<T>) {
        writer = smartWriter
    }

    fun build() {

    }
}
