package dorving.karth.message

import dorving.karth.api.Offers
import gearth.extensions.parsers.*
import gearth.extensions.parsers.navigator.HNavigatorSearchResult
import gearth.protocol.HPacket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime

/**
 * Represents a decoder for [messages][M].
 */
@Suppress("unused")
sealed interface MessageDecoder<M : Message> {

    /**
     * Decode a [message][M] from a [packet].
     */
    fun decode(packet: HPacket): M

    /**
     * Throws when no [MessageDecoder] was found for messages of class [messageKClass].
     */
    class NotFoundException(messageKClass: KClass<out Message>) :
        Exception("No decoder found for $messageKClass")

    /**
     * Throws when [decoding][MessageDecoder.decode] of [packet] failed.
     */
    class DecodeException(packet: HPacket, messageClass: KClass<out Message>, exception: java.lang.Exception) :
        Exception("Failed to decode $messageClass from $packet", exception)


    object IncomingChatDecoder : MessageDecoder<Message.Incoming.Chat> {
        override fun decode(packet: HPacket) =
            Message.Incoming.Chat(
                userIndex = packet.readInteger(),
                contents = packet.readString())
    }

    object OutgoingChatDecoder : MessageDecoder<Message.Outgoing.Chat> {
        override fun decode(packet: HPacket) =
            Message.Outgoing.Chat(
                userIndex = packet.readInteger(),
                contents = packet.readString(),
                color = packet.readInteger())
    }

    object IncomingShoutDecoder : MessageDecoder<Message.Incoming.Shout> {
        override fun decode(packet: HPacket) =
            Message.Incoming.Shout(
                userIndex = packet.readInteger(),
                message = packet.readString())
    }

    object OutgoingShoutDecoder : MessageDecoder<Message.Outgoing.Shout> {
        override fun decode(packet: HPacket) =
            Message.Outgoing.Shout(
                message = packet.readString(),
                userIndex = packet.readInteger())
    }

    object IncomingWhisperDecoder : MessageDecoder<Message.Incoming.Whisper> {
        override fun decode(packet: HPacket) =
            Message.Incoming.Whisper(
                userIndex = packet.readInteger(),
                message = packet.readString())
    }

    object OutgoingWhisperDecoder : MessageDecoder<Message.Outgoing.Whisper> {
        override fun decode(packet: HPacket) =
            Message.Outgoing.Whisper(
                message = packet.readString(),
                userIndex = packet.readInteger())
    }

    object OutgoingSendMsgDecoder : MessageDecoder<Message.Outgoing.SendMsg> {
        override fun decode(packet: HPacket) =
            Message.Outgoing.SendMsg(
                recipientId = packet.readInteger(),
                message = packet.readString())
    }

    object IncomingRoomReadyDecoder : MessageDecoder<Message.Incoming.RoomReady> {
        override fun decode(packet: HPacket) = Message.Incoming.RoomReady()
    }

    object IncomingGetGuestRoomResultDecoder : MessageDecoder<Message.Incoming.GetGuestRoomResult> {
        override fun decode(packet: HPacket) =
            Message.Incoming.GetGuestRoomResult(
                arg1 = packet.readBoolean(),
                roomId = packet.readInteger(),
                roomName = packet.readString(),
                roomOwnerId = packet.readInteger(),
                roomOwnerName = packet.readString(),
                arg6 = packet.readInteger(),
                arg7 = packet.readInteger(),
                arg8 = packet.readInteger(),
                roomDescription = packet.readString(),
                arg10 = packet.readInteger(),
                roomRating = packet.readInteger()
            )
    }

    object IncomingUsersDecoder : MessageDecoder<Message.Incoming.Users> {
        override fun decode(packet: HPacket) =
            Message.Incoming.Users(entities = HEntity.parse(packet))
    }

    object IncomingUserRemoveDecoder : MessageDecoder<Message.Incoming.UserRemove> {
        override fun decode(packet: HPacket) =
            Message.Incoming.UserRemove(userIndex = packet.readInteger())
    }


    object IncomingObjectsDecoder : MessageDecoder<Message.Incoming.Objects> {
        override fun decode(packet: HPacket) =
            Message.Incoming.Objects(floorItems = HFloorItem.parse(packet))
    }

    object IncomingObjectAddDecoder : MessageDecoder<Message.Incoming.ObjectAdd> {
        override fun decode(packet: HPacket) =
            Message.Incoming.ObjectAdd(floorItem = HFloorItem(packet))
    }

    object IncomingObjectRemoveDecoder : MessageDecoder<Message.Incoming.ObjectRemove> {
        override fun decode(packet: HPacket) =
            Message.Incoming.ObjectRemove(
                floorItemId = packet.readInteger(),
                arg2 = packet.readBoolean(),
                furniOwnerId = packet.readInteger(),
                arg4 = packet.readInteger())
    }

    object IncomingObjectUpdateDecoder : MessageDecoder<Message.Incoming.ObjectUpdate> {
        override fun decode(packet: HPacket) =
            Message.Incoming.ObjectUpdate(floorItem = HFloorItem(packet))
    }

    object IncomingObjectDataUpdateDecoder : MessageDecoder<Message.Incoming.ObjectDataUpdate> {
        override fun decode(packet: HPacket): Message.Incoming.ObjectDataUpdate {
            val floorItemId = packet.readString().toInt()
            val category = packet.readInteger()
            return Message.Incoming.ObjectDataUpdate(floorItemId, category, HStuff.readData(packet, category))
        }
    }

    @ExperimentalTime
    @DelicateCoroutinesApi
    @ExperimentalSerializationApi
    object IncomingGetOwnOffersDecoder : MessageDecoder<Message.Incoming.GetOwnOffersResult> {
        override fun decode(packet: HPacket) =
            Message.Incoming.GetOwnOffersResult(offers = Offers.Own.parse(packet))
    }

    @ExperimentalTime
    @DelicateCoroutinesApi
    @ExperimentalSerializationApi
    object IncomingGetOffersDecoder : MessageDecoder<Message.Incoming.GetOffersResult> {
        override fun decode(packet: HPacket) =
            Message.Incoming.GetOffersResult(offers = Offers.All.parse(packet))
    }

    object IncomingMakeOfferResultDecoder : MessageDecoder<Message.Incoming.MakeOfferResult> {
        override fun decode(packet: HPacket) =
            when (val responseCode = packet.readInteger()) {
                1 -> Message.Incoming.MakeOfferResult.Success
                2 -> Message.Incoming.MakeOfferResult.Failure
                else -> throw Exception("Unexpected response code $responseCode for `MarketplaceMakeOfferResult`.")
            }
    }

    object IncomingFurniListDecoder : MessageDecoder<Message.Incoming.FurniList> {
        override fun decode(packet: HPacket) =
            Message.Incoming.FurniList(HInventoryItem.parse(packet))
    }

    object IncomingFurniListAddOrUpdateDecoder : MessageDecoder<Message.Incoming.FurniListAddOrUpdate> {
        override fun decode(packet: HPacket) =
            Message.Incoming.FurniListAddOrUpdate(
                furniUniqueId = packet.readInteger(),
                furniProductType = HProductType.fromString(packet.readString()),
                furniUniqueIdUnsigned = packet.readInteger(),
                furniTypeId = packet.readInteger(),
                arg5 = packet.readInteger(),
                arg6 = packet.readInteger(),
                arg7 = packet.readInteger(),
                arg8 = packet.readBoolean(),
                arg9 = packet.readBoolean(),
                arg10 = packet.readInteger(),
                arg11 = packet.readBoolean(),
                arg12 = packet.readInteger(),
                arg13 = packet.readInteger(),
                arg14 = packet.readString()
            )
    }

    object IncomingCreditBalanceDecoder : MessageDecoder<Message.Incoming.CreditBalance> {
        override fun decode(packet: HPacket) =
            Message.Incoming.CreditBalance(balance =  packet.readString().toDouble().toInt())
    }

    object IncomingNavigatorSearchResultDecoder : MessageDecoder<Message.Incoming.NavigatorSearchResults> {
        override fun decode(packet: HPacket) =
            Message.Incoming.NavigatorSearchResults(result = HNavigatorSearchResult(packet))
    }
}
