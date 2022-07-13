package karth.plugin

import gearth.misc.HostInfo
import gearth.protocol.HMessage
import gearth.protocol.connection.HClient
import gearth.services.extension_handler.extensions.implementations.network.NetworkExtensionInfo.INCOMING_MESSAGES_IDS
import gearth.services.extension_handler.extensions.implementations.network.NetworkExtensionInfo.OUTGOING_MESSAGES_IDS
import gearth.services.packet_info.PacketInfoManager
import karth.plugin.ExtensionMessage.Incoming
import karth.plugin.ExtensionMessage.Outgoing
import karth.core.protocol.ClassPacketStructureMap
import karth.core.protocol.NamePacketStructureMap
import karth.core.protocol.PacketStructureCodec

object ExtensionCodec {

    private val codec = PacketStructureCodec(
        ClassPacketStructureMap().apply { registerOutgoing() },
        NamePacketStructureMap().apply { registerIncoming() }
    )

    val incomingMap = codec.client.structures.values.associateBy { it.headerId }
    val outgoingMap = codec.server

    private fun NamePacketStructureMap.registerIncoming() {
        register<Incoming.InfoRequest> {
            headerId = OUTGOING_MESSAGES_IDS.INFOREQUEST
            read {
                Incoming.InfoRequest
            }
        }
        register<Incoming.ConnectionStart> {
            headerId = OUTGOING_MESSAGES_IDS.CONNECTIONSTART
            read {
                Incoming.ConnectionStart(
                    host = readString(),
                    connectionPort = readInteger(),
                    hotelVersion = readString(),
                    clientIdentifier = readString(),
                    clientType = HClient.valueOf(readString()),
                    packetInfoManager = PacketInfoManager.readFromPacket(this)
                )
            }
        }
        register<Incoming.ConnectionEnd> {
            headerId = OUTGOING_MESSAGES_IDS.CONNECTIONEND
            read {
                Incoming.ConnectionEnd
            }
        }
        register<Incoming.FlagsCheck> {
            headerId = OUTGOING_MESSAGES_IDS.FLAGSCHECK
            read {
                Incoming.FlagsCheck(flags = List(size = readInteger(), init = ::readString))
            }
        }
        register<Incoming.Init> {
            headerId = OUTGOING_MESSAGES_IDS.INIT
            read {
                Incoming.Init(delayInit = readBoolean(), hostInfo = HostInfo.fromPacket(this))
            }
        }
        register<Incoming.OnDoubleClick> {
            headerId = OUTGOING_MESSAGES_IDS.ONDOUBLECLICK
            read {
                Incoming.OnDoubleClick
            }
        }
        register<Incoming.PacketIntercept> {
            headerId = OUTGOING_MESSAGES_IDS.PACKETINTERCEPT
            read {
                Incoming.PacketIntercept(readLongString())
            }
        }
        register<Incoming.UpdateHostInfo> {
            headerId = OUTGOING_MESSAGES_IDS.UPDATEHOSTINFO
            read {
                Incoming.UpdateHostInfo(hostInfo = HostInfo.fromPacket(this))
            }
        }
    }

    private fun ClassPacketStructureMap.registerOutgoing() {
        register<Outgoing.ExtensionInfo> {
            headerId = INCOMING_MESSAGES_IDS.EXTENSIONINFO
            write {
                it.appendString(title)
                it.appendString(author)
                it.appendString(version)
                it.appendString(description)
                it.appendBoolean(onClickUsed)
                it.appendBoolean(file != null)
                it.appendString(file ?: "")
                it.appendString(cookie ?: "")
                it.appendBoolean(canLeave)
                it.appendBoolean(canDelete)
            }
        }
        register<Outgoing.ManipulatedPacket> {
            headerId = INCOMING_MESSAGES_IDS.MANIPULATEDPACKET
            write {
                it.appendLongString(hMessage.stringify())
            }
        }
        register<Outgoing.SendMessage> {
            headerId = INCOMING_MESSAGES_IDS.SENDMESSAGE
            write {
                it.appendByte(when (direction) {
                    HMessage.Direction.TOCLIENT -> 0.toByte()
                    HMessage.Direction.TOSERVER -> 1.toByte()
                })
                it.appendInt(packet.bytesLength)
                it.appendBytes(packet.toBytes())
            }
        }
        register<Outgoing.RequestFlags> {
            headerId = INCOMING_MESSAGES_IDS.REQUESTFLAGS
        }
        register<Outgoing.ExtensionConsoleLog> {
            headerId = INCOMING_MESSAGES_IDS.EXTENSIONCONSOLELOG
            write {
                it.appendString(contents)
            }
        }
    }
}
