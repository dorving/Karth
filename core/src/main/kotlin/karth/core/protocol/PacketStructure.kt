package karth.core.protocol

import com.github.michaelbull.logging.InlineLogger
import gearth.protocol.HPacket
import gearth.services.packet_info.PacketInfo

private typealias PacketWriter<T> = T.(HPacket) -> Unit
private typealias PacketReader<T> = HPacket.(PacketInfo?) -> T

/**
 * Represents a generic packet structure for [outgoing packets][ServerPacket].
 *
 * @property T the type of [ServerPacket]
 *
 * @param headerId required when name-headerId pair is not known to G-Earth, by default is [HEADER_ID_UNDEFINED].
 * @param name required to find [PacketInfo] in G-Earth.
 * @param write a [PacketWriter] to encode [T] as a [HPacket].
 */
class PacketStructure<T : Packet>(
    val headerId: Int,
    val name: String,
    val write: PacketWriter<T>,
    val read: PacketReader<T>?,
)

@DslMarker
private annotation class BuilderDslMarker

const val HEADER_ID_UNDEFINED = -1

private val logger = InlineLogger("PacketBuilder")
@BuilderDslMarker
class PacketBuilder<T : Packet>(private val name: String) {

    private lateinit var packetWriter: PacketWriter<T>
    private var packetReader: PacketReader<T>? = null

    var headerId: Int = HEADER_ID_UNDEFINED

    fun write(write: PacketWriter<T>) {
        this.packetWriter = write
    }

    fun read(reader: PacketReader<T>) {
        this.packetReader = reader
    }

    fun build(): PacketStructure<T> {
        if (!::packetWriter.isInitialized)
            packetWriter = { }
        if (packetReader == null)
            logger.warn { "Did not define reader for message (name=$name)" }
        return PacketStructure(
            headerId = headerId,
            name = name,
            write = packetWriter,
            read = packetReader
        )
    }
}