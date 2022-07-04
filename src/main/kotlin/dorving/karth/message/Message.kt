package dorving.karth.message

import dorving.karth.api.Offers
import gearth.extensions.parsers.HEntity
import gearth.extensions.parsers.HFloorItem
import gearth.extensions.parsers.HInventoryItem
import gearth.extensions.parsers.HProductType
import gearth.extensions.parsers.navigator.HNavigatorSearchResult
import gearth.protocol.HMessage
import gearth.protocol.HPacket
import gearth.services.packet_info.PacketInfo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime

sealed class Message(val name: String, val direction: HMessage.Direction) {

    lateinit var packetInfo: PacketInfo
    lateinit var hMessage: HMessage

    fun toPacket(vararg arguments: Any) =
        HPacket(name, direction, *arguments)

    annotation class Header(val name: String)
    annotation class Encoder(val encoderClass: KClass<out MessageEncoder<*>>)
    annotation class Decoder(val decoderClass: KClass<out MessageDecoder<*>>)

    /**
     * Thrown when no [Message] was found the messages of super class [superClass] for [info].
     */
    class NotFoundException(superClass: KClass<out Message>, info: PacketInfo)
        : Exception("Failed to find Message class of class $superClass for $info")

    /**
     * Thrown when habbo session failed to handle the [message].
     */
    class HandleException(message: Message, exception: java.lang.Exception)
        : Exception("Failed to handle $message", exception)

    sealed class Incoming(name: String) : Message(name, HMessage.Direction.TOCLIENT) {

        @Header("Chat")
        @Encoder(MessageEncoder.IncomingChatEncoder::class)
        @Decoder(MessageDecoder.IncomingChatDecoder::class)
        data class Chat(val userIndex: Int, val contents: String) : Incoming("Chat")

        @Header("Shout")
        @Encoder(MessageEncoder.IncomingShoutEncoder::class)
        @Decoder(MessageDecoder.IncomingShoutDecoder::class)
        data class Shout(val userIndex: Int, val message: String) : Incoming("Shout")

        @Header("Whisper")
        @Encoder(MessageEncoder.IncomingWhisperEncoder::class)
        @Decoder(MessageDecoder.IncomingWhisperDecoder::class)
        data class Whisper(val userIndex: Int, val message: String) : Incoming("Whisper")

        @Header("RoomReady")
        @Encoder(MessageEncoder.IncomingRoomReadyEncoder::class)
        @Decoder(MessageDecoder.IncomingRoomReadyDecoder::class)
        class RoomReady : Incoming("RoomReady")

        /**
         * TODO: add missing fields
         */
        @Header("GetGuestRoomResult")
//        @Encoder(MessageEncoder.IncomingGetGuestRoomResultEncoder::class)
        @Decoder(MessageDecoder.IncomingGetGuestRoomResultDecoder::class)
        data class GetGuestRoomResult(
            val arg1: Boolean,
            val roomId: Int,
            val roomName: String,
            val roomOwnerId: Int,
            val roomOwnerName: String,
            val arg6: Int,
            val arg7: Int,
            val arg8: Int,
            val roomDescription: String,
            val arg10: Int,
            val roomRating: Int
        ) : Incoming("GetGuestRoomResult")

        @Header("Users")
        @Encoder(MessageEncoder.IncomingUsersEncoder::class)
        @Decoder(MessageDecoder.IncomingUsersDecoder::class)
        data class Users(val entities: Array<HEntity>) : Incoming("Users")

        @Header("UserRemove")
        @Encoder(MessageEncoder.IncomingUserRemoveEncoder::class)
        @Decoder(MessageDecoder.IncomingUserRemoveDecoder::class)
        data class UserRemove(val userIndex: Int) : Incoming("UserRemove")

        @Header("Objects")
        @Decoder(MessageDecoder.IncomingObjectsDecoder::class)
        data class Objects(val floorItems: Array<HFloorItem>) : Incoming("Objects")

        @Header("ObjectAdd")
        @Encoder(MessageEncoder.IncomingObjectAddEncoder::class)
        @Decoder(MessageDecoder.IncomingObjectAddDecoder::class)
        data class ObjectAdd(val floorItem: HFloorItem) : Incoming("ObjectAdd")

        @Header("ObjectRemove")
        @Decoder(MessageDecoder.IncomingObjectAddDecoder::class)
        data class ObjectRemove(val floorItemId: Int, val arg2: Boolean, val furniOwnerId: Int, val arg4: Int)
            : Incoming("ObjectRemove")

        @Header("ObjectUpdate")
        @Decoder(MessageDecoder.IncomingObjectUpdateDecoder::class)
        data class ObjectUpdate(val floorItem: HFloorItem) : Incoming("ObjectUpdate")

        @Header("ObjectDataUpdate")
//        @Encoder(MessageEncoder.IncomingObjectDataUpdateEncoder::class)
        @Decoder(MessageDecoder.IncomingObjectDataUpdateDecoder::class)
        data class ObjectDataUpdate(val floorItemId: Int, val category: Int, val data: Array<Any> = emptyArray()) : Incoming("ObjectDataUpdate")

        @ExperimentalTime
        @DelicateCoroutinesApi
        @ExperimentalSerializationApi
        @Header("MarketPlaceOwnOffers")
        @Decoder(MessageDecoder.IncomingGetOwnOffersDecoder::class)
        data class GetOwnOffersResult(val offers: Offers.Own) : Incoming("MarketPlaceOwnOffers")

        @ExperimentalTime
        @DelicateCoroutinesApi
        @ExperimentalSerializationApi
        @Header("MarketPlaceOffers")
        @Decoder(MessageDecoder.IncomingGetOffersDecoder::class)
        data class GetOffersResult(val offers: Offers.All) : Incoming("MarketPlaceOffers")

        @Header("MarketplaceMakeOfferResult")
        @Decoder(MessageDecoder.IncomingMakeOfferResultDecoder::class)
        sealed class MakeOfferResult(val responseCode: Int) : Incoming("MarketplaceMakeOfferResult") {
            object Success : MakeOfferResult(responseCode = 1)
            object Failure : MakeOfferResult(responseCode = 2)
        }

        @Header("FurniList")
        @Decoder(MessageDecoder.IncomingFurniListDecoder::class)
        data class FurniList(val inventoryItems: Array<HInventoryItem>) : Incoming("FurniList")

        @Header("FurniListAddOrUpdate")
        @Decoder(MessageDecoder.IncomingFurniListAddOrUpdateDecoder::class)
        data class FurniListAddOrUpdate(
            val furniUniqueId: Int,
            val furniProductType: HProductType,
            val furniUniqueIdUnsigned: Int,
            val furniTypeId: Int,
            val arg5: Int,
            val arg6: Int,
            val arg7: Int,
            val arg8: Boolean,
            val arg9: Boolean,
            val arg10: Int,
            val arg11: Boolean,
            val arg12: Int,
            val arg13: Int,
            val arg14: String
        ) : Incoming("FurniListAddOrUpdate")

        @Header("CreditBalance")
        @Decoder(MessageDecoder.IncomingCreditBalanceDecoder::class)
        data class CreditBalance(val balance: Int) : Incoming("CreditBalance")

        @Header("NavigatorSearchResultBlocks")
        @Decoder(MessageDecoder.IncomingNavigatorSearchResultDecoder::class)
        data class NavigatorSearchResults(val result: HNavigatorSearchResult) : Incoming("NavigatorSearchResultBlocks")
    }

    sealed class Outgoing(name: String) : Message(name, HMessage.Direction.TOSERVER) {

        @Header("Chat")
        @Encoder(MessageEncoder.OutgoingChatEncoder::class)
        data class Chat(val userIndex: Int, val contents: String, val color: Int) : Outgoing("Chat")

        @Header("Shout")
        @Encoder(MessageEncoder.OutgoingShoutEncoder::class)
        data class Shout(val message: String, val userIndex: Int) : Outgoing("Shout")

        @Header("Whisper")
        @Encoder(MessageEncoder.OutgoingWhisperEncoder::class)
        data class Whisper(val message: String, val userIndex: Int) : Outgoing("Whisper")

        @Header("SendMsg")
        @Encoder(MessageEncoder.OutgoingSendMsgEncoder::class)
        data class SendMsg(val recipientId: Int, val message: String) : Outgoing("SendMsg")

        @Header("PlaceObject")
        @Encoder(MessageEncoder.OutgoingPlaceObjectEncoder::class)
        data class PlaceObject(val placementId: Int, val x: Int, val y: Int, val dir: Int) : Outgoing("PlaceObject")

        @Header("GetGuestRoom")
        @Encoder(MessageEncoder.OutgoingGetGuestRoomEncoder::class)
        data class GetGuestRoom(val roomId: Int, val arg2: Int, val arg3: Int) : Outgoing("GetGuestRoom")

        @Header("GetCreditsInfo")
        @Encoder(MessageEncoder.OutgoingGetCreditsInfoEncoder::class)
        object GetCreditsInfo : Outgoing("GetCreditsInfo")

        @Header("GetMarketplaceOwnOffers")
        @Encoder(MessageEncoder.OutgoingGetOwnOffersEncoder::class)
        object GetOwnOffers : Outgoing("GetMarketplaceOwnOffers")

        @Header("GetMarketplaceOffers")
        @Encoder(MessageEncoder.OutgoingGetOffersEncoder::class)
        data class GetOffers(val arg1: Int, val arg2: Int, val searchTerm: String, val sortType: Int)
            : Outgoing("GetMarketplaceOffers")

        @Header("BuyMarketplaceOffer")
        @Encoder(MessageEncoder.OutgoingBuyOfferEncoder::class)
        data class BuyOffer(val offerId: Int) : Outgoing("BuyMarketplaceOffer")

        @Header("CancelMarketplaceOffer")
        @Encoder(MessageEncoder.OutgoingCancelOfferEncoder::class)
        data class CancelOffer(val offerId: Int) : Outgoing("CancelMarketplaceOffer")

        @Header("MakeOffer")
        @Encoder(MessageEncoder.OutgoingMakeOfferEncoder::class)
        data class MakeOffer(val furniId: Int, val furniType: Int, val sellPrice: Int) : Outgoing("MakeOffer")

        @Header("UseFurniture")
        @Encoder(MessageEncoder.OutgoingUseFurnitureEncoder::class)
        data class UseFurniture(val furniId: Int, val someInt: Int = 0) : Outgoing("UseFurniture")

        @Header("RequestFurniInventory")
        @Encoder(MessageEncoder.OutgoingRequestFurniInventoryEncoder::class)
        object RequestFurniInventory : Outgoing("RequestFurniInventory")

        @Header("NewNavigatorSearch")
        @Encoder(MessageEncoder.OutgoingNewNavigatorSearchEncoder::class)
        data class NavigatorSearch(val category: String, val searchTerm: String) : Outgoing("NewNavigatorSearch")
    }
}
