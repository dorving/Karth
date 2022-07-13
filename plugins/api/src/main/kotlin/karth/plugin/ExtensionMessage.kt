package karth.plugin

import gearth.misc.HostInfo
import gearth.protocol.HMessage
import gearth.protocol.HMessage.Direction
import gearth.protocol.HPacket
import gearth.protocol.connection.HClient
import gearth.services.packet_info.PacketInfoManager
import karth.core.protocol.ClientPacket
import karth.core.protocol.ServerPacket

/**
 * Temporary until https://github.com/sirjonasxx/G-Earth/pull/137 is merged
 */
sealed class ExtensionMessage {

    sealed class Outgoing : ExtensionMessage(), ServerPacket {
        class ExtensionInfo(
            val title: String,
            val author: String,
            val version: String,
            val description: String,
            val onClickUsed: Boolean = false,
            val file: String?,
            val cookie: String?,
            val canLeave: Boolean = false,
            val canDelete: Boolean = false
        ) : Outgoing()

        object RequestFlags : Outgoing()
        class SendMessage(val packet: HPacket, val direction: Direction) : Outgoing()
        class PacketToStringRequest : Outgoing()
        class StringToPacketRequest : Outgoing()
        class ExtensionConsoleLog(val contents: String) : Outgoing()
        class ManipulatedPacket(val hMessage: HMessage) : Outgoing()
    }

    sealed class Incoming : ExtensionMessage(), ClientPacket {
        object OnDoubleClick : Incoming()
        object InfoRequest : Incoming()
        class PacketIntercept(val packetString: String) : Incoming()
        class FlagsCheck(val flags: List<String>) : Incoming()
        class ConnectionStart(
            val host: String,
            val connectionPort: Int,
            val hotelVersion: String,
            val clientIdentifier: String,
            val clientType: HClient,
            val packetInfoManager: PacketInfoManager
        ) : Incoming()
        object ConnectionEnd : Incoming()
        class Init(val delayInit: Boolean, val hostInfo: HostInfo) : Incoming()
        class UpdateHostInfo(val hostInfo: HostInfo) : Incoming()
        class PacketToStringResponse : Incoming()
        class StringToPacketResponse : Incoming()
    }
}
