package karth.protocol.habbo

import gearth.protocol.connection.HClient
import karth.core.message.Message
import karth.core.message.Message.Incoming
import karth.util.appendStringUTF8

fun registerUnity(codec: HabboCodec) {
    registerOutgoing(codec)
    registerIncoming(codec)
}

private fun registerIncoming(codec: HabboCodec) {
    codec.incoming(clientType = HClient.UNITY).apply {
        register<Incoming.Chat> {
            read {
                Incoming.Chat(
                    userIndex = readInteger(),
                    contents = readString(),
                    arg3 = readInteger(),
                    bubble = readInteger(),
                    arg5 = readInteger(),
                    count = readInteger()
                )
            }
        }
        register<Incoming.Shout> {
            read {
                Incoming.Shout(
                    userIndex = readInteger(),
                    contents = readString(),
                    arg3 = readInteger(),
                    bubble = readInteger(),
                    arg5 = readInteger(),
                    count = readInteger()
                )
            }
        }
        register<Incoming.Whisper> {
            read {
                Incoming.Whisper(
                    userIndex = readInteger(),
                    contents = readString(),
                    arg3 = readInteger(),
                    bubble = readInteger(),
                    arg5 = readInteger(),
                    count = readInteger()
                )
            }
        }
        register<Incoming.RoomReady> {
            read {
                Incoming.RoomReady(roomType = readString(), roomId = readLong())
            }
        }
        register<Incoming.GetGuestRoomResult> {
            read {
                val enterRoom = readBoolean()
                val id = readInteger()
                val name = readString()
                val ownerId = readInteger()
                val ownerName = readString()
                val doorMode = readInteger()
                val userCount = readInteger()
                val maxUserCount = readInteger()
                val description = readString()
                val tradeMode = readInteger()
                val score = readInteger()
                val ranking = readInteger()
                val categoryId = readInteger()
                val tagCount = readUshort()
                val multiUse = readInteger()
                val roomForward = readBoolean()
                val staffPick = readBoolean()
                val isGroupMember = readBoolean()
                val allInRoomMuted = readBoolean()
                val whoCanMute = readInteger()
                val whoCanKick = readInteger()
                val whoCanBan = readInteger()
                val canMute = readBoolean()
                val chatMode = readInteger()
                val chatBubbleWidth = readInteger()
                val chatScrollSpeed = readInteger()
                val chatFullHearRange = readInteger()
                val chatFloodSensitivity = readInteger()
                val undefined = readInteger()
                val undefined2 = readInteger()
                Incoming.GetGuestRoomResult(enterRoom,
                    id,
                    name,
                    ownerId,
                    ownerName,
                    doorMode,
                    userCount,
                    maxUserCount,
                    description,
                    tradeMode,
                    score)
            }
        }
    }
}

private fun registerOutgoing(codec: HabboCodec) {
    codec.outgoing(clientType = HClient.UNITY).apply {
        register<Message.Outgoing.Chat> {
            write {
                it.appendStringUTF8(contents)
                it.appendInt(bubble)
                it.appendInt(-1)
            }
        }
        register<Message.Outgoing.Shout> {
            write {
                it.appendStringUTF8(contents)
                it.appendInt(bubble)
            }
        }
        register<Message.Outgoing.Whisper> {
            write {
                it.appendStringUTF8(contents)
                it.appendInt(bubble)
            }
        }
        register<Message.Outgoing.SendMsg> {
            write {
                it.appendLong(recipientId.toLong())
                it.appendStringUTF8(contents)
            }
        }
        register<Message.Outgoing.PlaceObject>(name = "PlaceRoomItem") {
            write {
                it.appendLong(placementId.toLong())
                it.appendInt(x)
                it.appendInt(y)
                it.appendInt(dir)
            }
        }
        register<Message.Outgoing.GetGuestRoom> {
            write {
                it.appendLong(roomId.toLong())
                it.appendInt(arg2)
                it.appendInt(arg3)
            }
        }
        register<Message.Outgoing.GetOffers>(name = "MarketplaceSearchOffers") {
            write {
                it.appendInt(minPrice)
                it.appendInt(maxPrice)
                it.appendStringUTF8(searchTerm)
                it.appendInt(sortType)
            }
        }
        register<Message.Outgoing.BuyOffer> {
            headerId = 3056
            write {
                it.appendLong(offerId.toLong())
                it.appendInt(offerPrice!!)
                it.appendString(arg3!!) // price
            }
        }
        register<Message.Outgoing.MakeOffer>(name = "MarketplaceMakeOffer") {
            write {
                it.appendInt(sellPrice)
                it.appendInt(furniType)
                when (this) {
                    is Message.Outgoing.MakeOffer.Single -> {
                        it.appendUShort(1)
                        it.appendLong(offerId.toLong())
                    }
                    is Message.Outgoing.MakeOffer.Multi -> {
                        it.appendUShort(offerIds.size)
                        for (offerId in offerIds)
                            it.appendLong(offerId.toLong())
                    }
                    else -> throw Exception("Unsupported type (this=$this)")
                }
            }
        }
        register<Message.Outgoing.CancelOffer>(name = "MarketplaceCancelOffer") {
            write {
                it.appendLong(offerId.toLong())
            }
        }
        register<Message.Outgoing.CancelAllOffers> {
            headerId = 3057
            write {}
        }
        register<Message.Outgoing.UseFurniture>(name = "UseStuff") {
            write {
                it.appendLong(furniId.toLong())
                it.appendInt(arg2)
            }
        }
    }
}
