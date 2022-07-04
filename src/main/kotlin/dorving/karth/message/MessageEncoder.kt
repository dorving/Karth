package dorving.karth.message

import gearth.protocol.HPacket

sealed class MessageEncoder<M : Message> {

    abstract fun encode(message: M): HPacket

    /**
     * Throws when no [MessageEncoder] was found for [message].
     */
    class NotFoundException(message: Message) :
        Exception("No encoder found for $message")

    open class EmptyEncoder<M : Message> : MessageEncoder<M>() {
        override fun encode(message: M) = message.toPacket()
    }

    object IncomingChatEncoder : MessageEncoder<Message.Incoming.Chat>() {
        override fun encode(message: Message.Incoming.Chat) =
            message.toPacket(message.userIndex, message.contents)
    }

    object OutgoingChatEncoder : MessageEncoder<Message.Outgoing.Chat>() {
        override fun encode(message: Message.Outgoing.Chat) =
            message.toPacket(message.contents, message.color, message.userIndex)
    }

    object IncomingShoutEncoder : MessageEncoder<Message.Incoming.Shout>() {
        override fun encode(message: Message.Incoming.Shout) =
            message.toPacket(message.userIndex, message.message)
    }

    object OutgoingShoutEncoder : MessageEncoder<Message.Outgoing.Shout>() {
        override fun encode(message: Message.Outgoing.Shout) =
            message.toPacket(message.message, message.userIndex)
    }

    object IncomingWhisperEncoder : MessageEncoder<Message.Incoming.Whisper>() {
        override fun encode(message: Message.Incoming.Whisper) =
            message.toPacket(message.userIndex, message.message)
    }

    object OutgoingWhisperEncoder : MessageEncoder<Message.Outgoing.Whisper>() {
        override fun encode(message: Message.Outgoing.Whisper) =
            message.toPacket(message.message, message.userIndex)
    }

    object OutgoingSendMsgEncoder : MessageEncoder<Message.Outgoing.SendMsg>() {
        override fun encode(message: Message.Outgoing.SendMsg) =
            message.toPacket(message.recipientId, message.message)
    }

    object IncomingRoomReadyEncoder : EmptyEncoder<Message.Incoming.RoomReady>()

//    object IncomingGetGuestRoomResultEncoder : MessageEncoder<Message.Incoming.GetGuestRoomResult>() {
//        override fun encode(message: Message.Incoming.GetGuestRoomResult) =
//            message.toPacket(message.arg1, message.arg2, message.roomName)
//    }

    object IncomingUsersEncoder : MessageEncoder<Message.Incoming.Users>() {
        override fun encode(message: Message.Incoming.Users) =
            message.toPacket().apply {
                val users = message.entities
                appendInt(users.size)
                users.forEach { it.appendToPacket(this) }
            }
    }

    object IncomingUserRemoveEncoder : MessageEncoder<Message.Incoming.UserRemove>() {
        override fun encode(message: Message.Incoming.UserRemove) =
            message.toPacket(message.userIndex)
    }

    object IncomingObjectAddEncoder : MessageEncoder<Message.Incoming.ObjectAdd>() {
        override fun encode(message: Message.Incoming.ObjectAdd) =
            message.toPacket().apply(message.floorItem::appendToPacket)
    }

//    object IncomingObjectDataUpdateEncoder : MessageEncoder<Message.Incoming.ObjectDataUpdate>() {
//        override fun encode(message: Message.Incoming.ObjectDataUpdate) =
//            message.toPacket(message.floorItemId, message.category, message.data)
//    }

    object OutgoingPlaceObjectEncoder : MessageEncoder<Message.Outgoing.PlaceObject>() {
        override fun encode(message: Message.Outgoing.PlaceObject) =
            message.toPacket(String.format("-%d %d %d %d", message.placementId, message.x, message.y, message.dir))
    }

    object OutgoingGetGuestRoomEncoder : MessageEncoder<Message.Outgoing.GetGuestRoom>() {
        override fun encode(message: Message.Outgoing.GetGuestRoom) =
            message.toPacket(message.roomId, message.arg2, message.arg3)
    }

    object OutgoingGetCreditsInfoEncoder : EmptyEncoder<Message.Outgoing.GetCreditsInfo>()


    object OutgoingGetOffersEncoder : MessageEncoder<Message.Outgoing.GetOffers>() {
        override fun encode(message: Message.Outgoing.GetOffers) =
            message.toPacket(message.arg1, message.arg2, message.searchTerm, message.sortType)
    }

    object OutgoingGetOwnOffersEncoder : EmptyEncoder<Message.Outgoing.GetOwnOffers>()

    object OutgoingCancelOfferEncoder : MessageEncoder<Message.Outgoing.CancelOffer>() {
        override fun encode(message: Message.Outgoing.CancelOffer) =
            message.toPacket(message.offerId)
    }

    object OutgoingBuyOfferEncoder : MessageEncoder<Message.Outgoing.BuyOffer>() {
        override fun encode(message: Message.Outgoing.BuyOffer) =
            message.toPacket(message.offerId)
    }

    object OutgoingMakeOfferEncoder : MessageEncoder<Message.Outgoing.MakeOffer>() {
        override fun encode(message: Message.Outgoing.MakeOffer) =
            message.toPacket(message.sellPrice, message.furniType, message.furniId)
    }

    object OutgoingUseFurnitureEncoder : MessageEncoder<Message.Outgoing.UseFurniture>() {
        override fun encode(message: Message.Outgoing.UseFurniture) =
            message.toPacket(message.furniId, message.someInt)
    }

    object OutgoingRequestFurniInventoryEncoder : EmptyEncoder<Message.Outgoing.RequestFurniInventory>()

    object OutgoingNewNavigatorSearchEncoder : MessageEncoder<Message.Outgoing.NavigatorSearch>() {
        override fun encode(message: Message.Outgoing.NavigatorSearch) =
            message.toPacket(message.category, message.searchTerm)
    }
}
