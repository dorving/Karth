package karth.core.message

import gearth.protocol.HMessage
import gearth.services.packet_info.PacketInfo
import karth.core.api.*
import karth.core.protocol.ClientPacket
import karth.core.protocol.ServerPacket

sealed class Message {

    lateinit var packetInfo: PacketInfo
    lateinit var hMessage: HMessage

    fun block() {
        hMessage.isBlocked = true
    }

    /**
     * Thrown when habbo session failed to handle the [message].
     */
    class HandleException(message: Message, exception: java.lang.Exception) :
        Exception("Failed to handle $message", exception)

    sealed class Incoming : Message(), ClientPacket {

        abstract class Speech : Incoming() {
            abstract val userIndex: Int
            abstract val contents: String
            abstract val arg3: Int
            abstract val bubble: Int
            abstract val arg5: String
            abstract val count: Int
        }

        @kotlinx.serialization.Serializable
        data class Chat(
            override val userIndex: Int,
            override val contents: String,
            override val arg3: Int,
            override val bubble: Int,
            override val arg5: String,
            override val count: Int
        ) : Speech()

        @kotlinx.serialization.Serializable
        data class Shout(
            override val userIndex: Int,
            override val contents: String,
            override val arg3: Int,
            override val bubble: Int,
            override val arg5: String,
            override val count: Int
        ) : Speech()

        @kotlinx.serialization.Serializable
        data class Whisper(
            override val userIndex: Int,
            override val contents: String,
            override val arg3: Int,
            override val bubble: Int,
            override val arg5: String,
            override val count: Int
        ) : Speech()

        @kotlinx.serialization.Serializable
        data class RoomReady(val roomType: String, val roomId: Long) : Incoming()


        sealed class RoomProperty : Incoming() {
            @kotlinx.serialization.Serializable
            class Floor(val value: Int) : RoomProperty()

            @kotlinx.serialization.Serializable
            class Landscape(val value: Double) : RoomProperty()
        }

        @kotlinx.serialization.Serializable
        data class RoomRating(val rating: Int, val arg2: Boolean) : Incoming()

        @kotlinx.serialization.Serializable
        data class YouAreController(val roomId: Long, val arg2: Int) : Incoming()

        @kotlinx.serialization.Serializable
        data class YouAreOwner(val roomId: Long) : Incoming()

        @kotlinx.serialization.Serializable
        data class GetGuestRoomResult(
            val enterRoom: Boolean,
            val id: Int,
            val name: String,
            val ownerId: Int,
            val ownerName: String,
            val doorMode: Int,
            val userCount: Int,
            val maxUserCount: Int,
            val description: String,
            val tradeMode: Int,
            val score: Int,
        ) : Incoming()


        @kotlinx.serialization.Serializable
        data class Users(val entities: List<Entity>) : Incoming()

        @kotlinx.serialization.Serializable
        data class UserRemove(val userIndex: Int) : Incoming()

        @kotlinx.serialization.Serializable
        data class Objects(val floorItems: List<FloorItem>) : Incoming()

        @kotlinx.serialization.Serializable
        data class ObjectAdd(val floorItem: FloorItem) : Incoming()

        @kotlinx.serialization.Serializable
        data class ObjectRemove(val floorItemId: Int, val arg2: Boolean, val furniOwnerId: Int, val arg4: Int) :
            Incoming()

        @kotlinx.serialization.Serializable
        data class ObjectUpdate(val floorItem: FloorItem) : Incoming()

        @kotlinx.serialization.Serializable
        data class ObjectDataUpdate(val floorItemId: Int, val category: Int, val data: Stuff) : Incoming()

        @kotlinx.serialization.Serializable
        data class GetOwnOffersResult(val offers: Offers.Own) : Incoming()

        @kotlinx.serialization.Serializable
        data class GetOffersResult(val offers: Offers.All) : Incoming()

        @kotlinx.serialization.Serializable
        sealed class MakeOfferResult(val responseCode: Int) : Incoming() {
            @kotlinx.serialization.Serializable
            object Success : MakeOfferResult(responseCode = 1)

            @kotlinx.serialization.Serializable
            object Failure : MakeOfferResult(responseCode = 2)
        }

        @kotlinx.serialization.Serializable
        data class FurniList(val inventoryItems: List<InventoryItem>) : Incoming()

        @kotlinx.serialization.Serializable
        data class FurniListRemove(val furniUniqueId: Int) : Incoming()

        @kotlinx.serialization.Serializable
        data class FurniListAddOrUpdate(
            val furniUniqueId: Int,
            val furniProductType: ProductType,
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
            val arg14: String,
        ) : Incoming()

        @kotlinx.serialization.Serializable
        data class CreditBalance(val balance: Int) : Incoming()

        @kotlinx.serialization.Serializable
        data class NavigatorSearchResults(val result: NavigatorSearchResult) : Incoming()

        @kotlinx.serialization.Serializable
        data class GetWardrobeResult(val arg1: Int, val lookEntries: List<Entry>) : Incoming() {

            @kotlinx.serialization.Serializable
            class Entry(val id: Int, val look: Look)
        }

        @kotlinx.serialization.Serializable
        data class GetNFTWardrobeResult(val arg1: Int) : Incoming()

        @kotlinx.serialization.Serializable
        data class GetHotLooksResult(val looks: List<Look>) : Incoming()

        @kotlinx.serialization.Serializable
        data class FigureUpdate(val look: Look) : Incoming()

        @kotlinx.serialization.Serializable
        data class TradingOpen(val myUserId: Int, val arg2: Int, val otherUserId: Int, val arg4: Int) : Incoming()

        @kotlinx.serialization.Serializable
        data class TradingAccept(val userId: Int, val userAction: Int) : Incoming() {
            fun userAccepts() = userAction > 0
        }
        @kotlinx.serialization.Serializable
        object TradingConfirmation : Incoming()

        @kotlinx.serialization.Serializable
        object TradingCompleted : Incoming()

        @kotlinx.serialization.Serializable
        data class TradingClose(val userId: Int, val reason: Int) : Incoming()
    }

    sealed class Outgoing : Message(), ServerPacket {

        abstract class Speech : Outgoing() {
            abstract val contents: String
            abstract val bubble: Int
        }

        @kotlinx.serialization.Serializable
        data class Chat(override val contents: String, override val bubble: Int = 0, val messageCount: Int = 0) : Speech()

        @kotlinx.serialization.Serializable
        data class Shout(override val contents: String, override val bubble: Int) : Speech()

        @kotlinx.serialization.Serializable
        data class Whisper(override val contents: String, override val bubble: Int) : Speech()

        @kotlinx.serialization.Serializable
        data class SendMsg(val recipientId: Int, val contents: String) : Outgoing()

        @kotlinx.serialization.Serializable
        data class PlaceObject(val placementId: Int, val x: Int, val y: Int, val dir: Int) : Outgoing()

        @kotlinx.serialization.Serializable
        data class GetGuestRoom(val roomId: Int, val arg2: Int, val arg3: Int) : Outgoing()

        @kotlinx.serialization.Serializable
        object GetCreditsInfo : Outgoing()

        @kotlinx.serialization.Serializable
        object GetOwnOffers : Outgoing()

        @kotlinx.serialization.Serializable
        data class GetOffers(val minPrice: Int, val maxPrice: Int, val searchTerm: String, val sortType: Int) :
            Outgoing()

        @kotlinx.serialization.Serializable
        data class BuyOffer(val offerId: Int, val offerPrice: Int? = null, val arg3: String? = null) : Outgoing()

        @kotlinx.serialization.Serializable
        data class CancelOffer(val offerId: Int) : Outgoing()

        @kotlinx.serialization.Serializable
        object CancelAllOffers : Outgoing()

        sealed class MakeOffer : Outgoing() {

            abstract val furniType: Int
            abstract val sellPrice: Int

            @kotlinx.serialization.Serializable
            data class Single(
                override val furniType: Int,
                override val sellPrice: Int,
                val offerId: Int
            ) : MakeOffer() {
                init {
                    require(sellPrice >= 2) { "Provided sellPrice must be >= 2" }
                }
            }

            @kotlinx.serialization.Serializable
            data class Multi(
                override val furniType: Int,
                override val sellPrice: Int,
                val offerIds: IntArray
            ) : MakeOffer() {
                init {
                    require(sellPrice >= 2) { "Provided sellPrice must be >= 2" }
                    require(offerIds.isNotEmpty()) { "Must provide at least one item id" }
                }

                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (javaClass != other?.javaClass) return false

                    other as Multi

                    if (!offerIds.contentEquals(other.offerIds)) return false

                    return true
                }

                override fun hashCode(): Int = offerIds.contentHashCode()
            }
        }

        @kotlinx.serialization.Serializable
        data class UseFurniture(val furniId: Int, val arg2: Int = 0) : Outgoing()

        @kotlinx.serialization.Serializable
        object RequestFurniInventory : Outgoing()

        @kotlinx.serialization.Serializable
        data class NavigatorSearch(val category: String, val searchTerm: String) : Outgoing()

        @kotlinx.serialization.Serializable
        object GetWardrobe : Outgoing()

        @kotlinx.serialization.Serializable
        object GetNFTWardrobe : Outgoing()

        @kotlinx.serialization.Serializable
        data class GetHotLooks(val arg1: Byte = 20.toByte()) : Outgoing()

        @kotlinx.serialization.Serializable
        data class UpdateFigureData(val look: Look) : Outgoing()

        @kotlinx.serialization.Serializable
        data class OpenTrading(val localUserId: Int) : Outgoing()

        @kotlinx.serialization.Serializable
        object AcceptTrading : Outgoing()

        @kotlinx.serialization.Serializable
        object ConfirmAcceptTrading : Outgoing()
    }
}
