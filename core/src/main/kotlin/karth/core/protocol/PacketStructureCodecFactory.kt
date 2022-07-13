package karth.core.protocol

import gearth.protocol.connection.HClient

open class PacketStructureCodecFactory {
    
    private val flash: PacketStructureCodec = codec()
    private val nitro: PacketStructureCodec = codec()
    private val unity: PacketStructureCodec = codec()
    
    fun incoming(clientType: HClient) = getCodec(clientType).client
    fun outgoing(clientType: HClient) = getCodec(clientType).server

    fun getCodec(device: HClient): PacketStructureCodec = when (device) {
        HClient.FLASH -> flash
        HClient.NITRO -> nitro
        HClient.UNITY -> unity
    }
    
    private fun codec() = PacketStructureCodec(
        ClassPacketStructureMap(),
        NamePacketStructureMap(),
    )
}
