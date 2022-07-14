//package karth.protocol.habbo
//
//import gearth.protocol.HPacket
//import gearth.protocol.connection.HClient
//import gearth.services.packet_info.PacketInfo
//import karth.core.api.Gender
//import karth.core.api.Look
//import karth.core.message.Message
//import karth.util.appendStringUTF8
//import karth.util.readStringUTF8
//import kotlin.math.absoluteValue
//
//HabboCodec.codec {
//
//    register<Message.Outgoing.Chat> { client ->
//        registerSpeech(client)
//    }
//    register<Message.Outgoing.Shout> { client ->
//        registerSpeech(client)
//    }
//    register<Message.Outgoing.Whisper> { client ->
//        registerSpeech(client)
//    }
//    register<Message.Outgoing.SendMsg> { client ->
//        write {
//            it.appendIntOrLong(recipientId, client)
//            it.appendStringUTF8(contents)
//        }
//        read {
//            Message.Outgoing.SendMsg(
//                recipientId = readIntOrLong(client),
//                contents = readStringUTF8(),
//            )
//        }
//    }
//    register<Message.Outgoing.PlaceObject> { client ->
//        name = when (client) {
//            HClient.UNITY -> "PlaceRoomItem"
//            HClient.FLASH -> Message.Outgoing.PlaceObject.asName()
//            HClient.NITRO -> TODO()
//        }
//        write {
//            when (client) {
//                HClient.UNITY -> {
//                    it.appendLong(placementId.toLong())
//                    it.appendInt(x)
//                    it.appendInt(y)
//                    it.appendInt(dir)
//                }
//                HClient.FLASH -> it.appendStringUTF8(String.format("-%d %d %d %d", placementId, x, y, dir))
//                HClient.NITRO -> TODO()
//            }
//        }
//        read {
//            when (client) {
//                HClient.UNITY -> Message.Outgoing.PlaceObject(
//                    placementId = readLong().toInt(),
//                    x = readInteger(),
//                    y = readInteger(),
//                    dir = readInteger()
//                )
//                HClient.FLASH -> readStringUTF8().split(" ").map { it.toInt() }.let {
//                    Message.Outgoing.PlaceObject(placementId = it[0].absoluteValue, x = it[1], y = it[2], dir = it[3])
//                }
//                HClient.NITRO -> TODO()
//            }
//        }
//    }
//    register<Message.Outgoing.GetGuestRoom> { client ->
//        write {
//            it.appendIntOrLong(roomId, client)
//            it.appendInt(arg2)
//            it.appendInt(arg3)
//        }
//        read {
//            Message.Outgoing.GetGuestRoom(
//                roomId = readIntOrLong(client),
//                arg2 = readInteger(),
//                arg3 = readInteger()
//            )
//        }
//    }
//    register<Message.Outgoing.GetCreditsInfo> { client ->
//        name = when (client) {
//            HClient.UNITY -> "GetCredits"
//            HClient.FLASH -> Message.Outgoing.GetCreditsInfo.asName()
//            HClient.NITRO -> TODO()
//        }
//        read { Message.Outgoing.GetCreditsInfo }
//        write { }
//    }
//    register<Message.Outgoing.GetOwnOffers> { client ->
//        name = when (client) {
//            HClient.UNITY -> "MarketplaceListOwnOffers"
//            HClient.FLASH -> "GetMarketplaceOwnOffers"
//            HClient.NITRO -> TODO()
//        }
//        read { Message.Outgoing.GetOwnOffers }
//        write { }
//    }
//    register<Message.Outgoing.GetOffers> { client ->
//        name = when (client) {
//            HClient.UNITY -> "MarketplaceSearchOffers"
//            HClient.FLASH -> "GetMarketplaceOffers"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            client.throwIfNitro()
//            it.appendInt(minPrice)
//            it.appendInt(maxPrice)
//            it.appendStringUTF8(searchTerm)
//            it.appendInt(sortType)
//        }
//        read {
//            client.throwIfNitro()
//            Message.Outgoing.GetOffers(
//                minPrice = readInteger(),
//                maxPrice = readInteger(),
//                searchTerm = readStringUTF8(),
//                sortType = readInteger()
//            )
//        }
//    }
//    register<Message.Outgoing.CancelOffer> { client ->
//        name = when(client) {
//            HClient.UNITY -> "MarketplaceCancelOffer"
//            HClient.FLASH -> "CancelOffer"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            it.appendIntOrLong(offerId, client)
//        }
//        read {
//            Message.Outgoing.CancelOffer(offerId = readIntOrLong(client))
//        }
//    }
//    register<Message.Outgoing.CancelAllOffers>(onlyFor = HClient.UNITY) {
//        headerId = 3057
//        read { Message.Outgoing.CancelAllOffers }
//        write {  }
//    }
//    register<Message.Outgoing.BuyOffer> { client ->
//        when (client) {
//            HClient.UNITY -> headerId = 3056
//            HClient.FLASH -> name = "BuyMarketplaceOffer"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            it.appendIntOrLong(offerId, client)
//            if (client == HClient.UNITY) {
//                it.appendInt(offerPrice!!)
//                it.appendString(arg3!!)
//            }
//        }
//        read {
//            Message.Outgoing.BuyOffer(
//                readIntOrLong(client),
//                if (client == HClient.NITRO) readInteger() else null,
//                if (client == HClient.NITRO) readString() else null,
//            )
//        }
//    }
//    register<Message.Outgoing.MakeOffer> { client ->
//        name = when (client) {
//            HClient.UNITY -> "MarketplaceMakeOffer"
//            HClient.FLASH -> "MakeOffer"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            it.appendInt(sellPrice)
//            it.appendInt(furniType)
//            when (this) {
//                is Message.Outgoing.MakeOffer.Single -> when (client) {
//                    HClient.UNITY -> {
//                        it.appendUShort(1)
//                        it.appendLong(offerId.toLong())
//                    }
//                    HClient.FLASH -> it.appendInt(offerId)
//                    HClient.NITRO -> TODO()
//                }
//                is Message.Outgoing.MakeOffer.Multi -> when (client) {
//                    HClient.UNITY -> {
//                        it.appendUShort(offerIds.size)
//                        for (offerId in offerIds)
//                            it.appendLong(offerId.toLong())
//                    }
//                    HClient.FLASH -> notSupported(client)
//                    HClient.NITRO -> TODO()
//                }
//                else -> throw Exception("Unsupported type (message=$this)")
//            }
//        }
//        read {
//            val sellPrice = readInteger()
//            val furniType = readInteger()
//            when(client) {
//                HClient.UNITY -> {
//                    val count = readUshort()
//                    if (count == 1)
//                        Message.Outgoing.MakeOffer.Single(sellPrice, furniType, offerId = readLong().toInt())
//                    else
//                        Message.Outgoing.MakeOffer.Multi(sellPrice, furniType, offerIds = IntArray(count) { readLong().toInt() })
//                }
//                HClient.FLASH -> Message.Outgoing.MakeOffer.Single(sellPrice, furniType, offerId = readInteger())
//                HClient.NITRO -> TODO()
//            }
//        }
//    }
//    register<Message.Outgoing.UseFurniture> { client ->
//        name = when(client) {
//            HClient.UNITY -> "UseStuff"
//            HClient.FLASH -> "UseFurniture"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            it.appendIntOrLong(furniId, client)
//            it.appendInt(arg2)
//        }
//        read {
//            Message.Outgoing.UseFurniture(furniId = readIntOrLong(client), arg2 = readInteger())
//        }
//    }
//    register<Message.Outgoing.RequestFurniInventory>(onlyFor = HClient.FLASH)
//    register<Message.Outgoing.NavigatorSearch> { client ->
//        name = when(client) {
//            HClient.UNITY -> "Navigator2Search"
//            HClient.FLASH -> "NewNavigatorSearch"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            it.appendStringUTF8(category)
//            it.appendStringUTF8(searchTerm)
//        }
//        read {
//            Message.Outgoing.NavigatorSearch(category = readStringUTF8(), searchTerm = readStringUTF8())
//        }
//    }
//    register<Message.Outgoing.GetWardrobe>(onlyFor = arrayOf(HClient.FLASH, HClient.UNITY))
//    register<Message.Outgoing.GetNFTWardrobe>(onlyFor = HClient.FLASH) {
//        name = "GetUserNftWardrobe"
//        read { Message.Outgoing.GetNFTWardrobe }
//        write {  }
//    }
//    register<Message.Outgoing.GetHotLooks>(onlyFor = arrayOf(HClient.FLASH, HClient.UNITY)) {
//        write {
//            it.appendByte(arg1)
//        }
//        read {
//            Message.Outgoing.GetHotLooks(arg1 = readByte())
//        }
//    }
//    register<Message.Outgoing.UpdateFigureData> { client ->
//        name = when(client) {
//            HClient.UNITY -> "UpdateAvatar"
//            HClient.FLASH -> "UpdateFigureData"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            it.appendString(look.gender.toString())
//            it.appendString(look.figureString)
//        }
//        read {
//            Message.Outgoing.UpdateFigureData(
//                Look(
//                    gender = Gender.fromString(readString())!!,
//                    figureString = readString()
//                )
//            )
//        }
//    }
//    register<Message.Outgoing.OpenTrading> { client ->
//        name = when(client) {
//            HClient.UNITY -> "TradeOpen"
//            HClient.FLASH -> "OpenTrading"
//            HClient.NITRO -> TODO()
//        }
//        write {
//            it.appendInt(localUserId)
//        }
//        read {
//            Message.Outgoing.OpenTrading(localUserId = readInteger())
//        }
//    }
//    register<Message.Outgoing.AcceptTrading> { client ->
//        name = when(client) {
//            HClient.UNITY -> "TradeAccept"
//            HClient.FLASH -> "AcceptTrading"
//            HClient.NITRO -> TODO()
//        }
//        write {  }
//        read { Message.Outgoing.AcceptTrading }
//    }
//    register<Message.Outgoing.ConfirmAcceptTrading> { client ->
//        name = when(client) {
//            HClient.UNITY -> "TradeConfirmAccept"
//            HClient.FLASH -> "ConfirmAcceptTrading"
//            HClient.NITRO -> TODO()
//        }
//        write {  }
//        read { Message.Outgoing.ConfirmAcceptTrading }
//    }
//}
//
//
//private fun<T : Message.Outgoing.Speech> Builder<T>.registerSpeech(client: HClient) {
//    write {
//        it.appendStringUTF8(contents)
//        it.appendInt(bubble)
//        if (this is Message.Outgoing.Chat) {
//            when (client) {
//                HClient.UNITY -> it.appendInt(-1)
//                HClient.FLASH -> it.appendInt(messageCount)
//                HClient.NITRO -> TODO()
//            }
//        }
//    }
//    read {
//        val contents = readStringUTF8()
//        val bubble = readInteger()
//        when {
//            it.named<Message.Outgoing.Chat>() -> Message.Outgoing.Chat(
//                contents, bubble,
//                messageCount = when (client) {
//                    HClient.UNITY, HClient.FLASH -> readInteger()
//                    HClient.NITRO -> TODO()
//                }
//            ) as T
//            it.named<Message.Outgoing.Whisper>() -> Message.Outgoing.Whisper(contents, bubble) as T
//            it.named<Message.Outgoing.Shout>() -> Message.Outgoing.Shout(contents, bubble) as T
//            else -> error("Unrecognised Outgoing.Speech packet, could not decode $it")
//        }
//    }
//}
//
//private fun Message.notSupported(client: HClient): Nothing = error("$this is not supported for $client")
//
//
//private fun HClient.throwIfNitro() = if (this == HClient.NITRO) TODO() else Unit
//
//private fun Any.asName(): String = this::class.simpleName!!
//
//private fun HPacket.readIntOrLong(client: HClient): Int = when (client) {
//    HClient.UNITY -> readLong().toInt()
//    HClient.FLASH -> readInteger()
//    HClient.NITRO -> TODO()
//}
//
//private fun HPacket.appendIntOrLong(value: Int, client: HClient) = when (client) {
//    HClient.UNITY -> appendLong(value.toLong())
//    HClient.FLASH -> appendInt(value)
//    HClient.NITRO -> TODO()
//}
//
//private inline fun <reified T> PacketInfo.named() = name == T::class.simpleName