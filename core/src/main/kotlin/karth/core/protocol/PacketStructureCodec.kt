package karth.core.protocol

import gearth.protocol.HMessage.Direction

class PacketStructureCodec(
    val server: ClassPacketStructureMap,
    val client: NamePacketStructureMap
) {
    fun forDirection(direction: Direction) = when(direction) {
        Direction.TOSERVER -> server
        Direction.TOCLIENT -> client
    }
}