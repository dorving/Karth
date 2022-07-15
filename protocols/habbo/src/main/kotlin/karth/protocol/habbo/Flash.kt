package karth.protocol.habbo

import gearth.extensions.parsers.HStuff
import gearth.protocol.connection.HClient
import karth.core.api.Gender
import karth.core.api.Look
import karth.core.api.Offers
import karth.core.api.ProductType
import karth.core.message.Message.Incoming
import karth.core.message.Message.Outgoing
import karth.util.appendStringUTF8
import karth.util.readStringUTF8
import kotlin.math.absoluteValue

@Suppress("RemoveExplicitTypeArguments")
fun registerFlash(codec: HabboCodec) {
    codec.outgoing(clientType = HClient.FLASH).apply {
        register<Outgoing.Chat> {
            write {
                it.appendStringUTF8(contents)
                it.appendInt(bubble)
                it.appendInt(messageCount)
            }
            read { Outgoing.Chat(contents = readStringUTF8(), bubble = readInteger(), messageCount = readInteger()) }
        }
        register<Outgoing.Shout> {
            write {
                it.appendStringUTF8(contents)
                it.appendInt(bubble)
            }
            read { Outgoing.Shout(contents = readStringUTF8(), bubble = readInteger()) }
        }
        register<Outgoing.Whisper> {
            write {
                it.appendStringUTF8(contents)
                it.appendInt(bubble)
            }
            read { Outgoing.Whisper(contents = readStringUTF8(), bubble = readInteger()) }
        }
        register<Outgoing.SendMsg> {
            write {
                it.appendInt(recipientId)
                it.appendStringUTF8(contents)
            }
            read { Outgoing.SendMsg(recipientId = readInteger(), contents = readStringUTF8()) }
        }
        register<Outgoing.PlaceObject> {
            write {
                it.appendStringUTF8(String.format("-%d %d %d %d", placementId, x, y, dir))
            }
            read {
                val args = readStringUTF8().split(" ").map { it.toInt() }
                Outgoing.PlaceObject(placementId = args[0].absoluteValue, x = args[1], y = args[2], dir = args[3])
            }
        }
        register<Outgoing.GetGuestRoom> {
            write {
                it.appendInt(roomId)
                it.appendInt(arg2)
                it.appendInt(arg3)
            }
            read {
                Outgoing.GetGuestRoom(roomId = readInteger(), arg2 = readInteger(), arg3 = readInteger())
            }
        }
        register<Outgoing.GetCreditsInfo>()
        register<Outgoing.GetOwnOffers>(name = "GetMarketplaceOwnOffers")
        register<Outgoing.GetOffers>(name = "GetMarketplaceOffers") {
            write {
                it.appendInt(minPrice)
                it.appendInt(maxPrice)
                it.appendStringUTF8(searchTerm)
                it.appendInt(sortType)
            }
            read {
                Outgoing.GetOffers(
                    minPrice = readInteger(),
                    maxPrice = readInteger(),
                    searchTerm = readStringUTF8(),
                    sortType = readInteger()
                )
            }
        }
        register<Outgoing.CancelOffer>(name = "CancelMarketplaceOffer") {
            write { it.appendInt(offerId) }
            read { Outgoing.CancelOffer(offerId = readInteger()) }
        }
        register<Outgoing.BuyOffer>(name = "BuyMarketplaceOffer") {
            write { it.appendInt(offerId) }
            read { Outgoing.BuyOffer(offerId = readInteger()) }
        }
        register<Outgoing.MakeOffer> {
            write {
                it.appendInt(sellPrice)
                it.appendInt(furniType)
                it.appendInt(
                    when (this) {
                        is Outgoing.MakeOffer.Single -> offerId
                        is Outgoing.MakeOffer.Multi -> offerIds[0]
                        else -> throw Exception("Unsupported type (message=$this)")
                    }
                )
            }
            read {
                Outgoing.MakeOffer.Single(
                    sellPrice = readInteger(),
                    furniType = readInteger(),
                    offerId = readInteger()
                )
            }
        }
        register<Outgoing.UseFurniture> {
            write {
                it.appendInt(furniId)
                it.appendInt(arg2)
            }
            read {
                Outgoing.UseFurniture(furniId = readInteger(), arg2 = readInteger())
            }
        }
        register<Outgoing.RequestFurniInventory>()
        register<Outgoing.NavigatorSearch>(name = "NewNavigatorSearch") {
            write {
                it.appendStringUTF8(category)
                it.appendStringUTF8(searchTerm)
            }
            read { Outgoing.NavigatorSearch(category = readStringUTF8(), searchTerm = readStringUTF8()) }
        }
        register<Outgoing.GetWardrobe>()
        register<Outgoing.GetNFTWardrobe>(name = "GetUserNftWardrobe")
        register<Outgoing.GetHotLooks>() {
            write { it.appendByte(arg1) }
            read { Outgoing.GetHotLooks(arg1 = readByte()) }
        }
        register<Outgoing.UpdateFigureData> {
            write {
                it.appendString(look.gender.toString())
                it.appendString(look.figureString)
            }
            read {
                Outgoing.UpdateFigureData(
                    Look(
                        gender = Gender.fromString(readString())!!,
                        figureString = readString()
                    )
                )
            }
        }
        register<Outgoing.OpenTrading> {
            write { it.appendInt(localUserId) }
            read { Outgoing.OpenTrading(readInteger()) }
        }
        register<Outgoing.AcceptTrading>()
        register<Outgoing.ConfirmAcceptTrading>()
    }
    codec.incoming(HClient.FLASH).apply {
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
            write {
                it.appendInt(userIndex)
                it.appendString(contents)
                it.appendInt(arg3)
                it.appendInt(bubble)
                it.appendInt(arg5)
                it.appendInt(count)
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
            write {
                it.appendInt(userIndex)
                it.appendString(contents)
                it.appendInt(arg3)
                it.appendInt(bubble)
                it.appendInt(arg5)
                it.appendInt(count)
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
            write {
                it.appendInt(userIndex)
                it.appendString(contents)
                it.appendInt(arg3)
                it.appendInt(bubble)
                it.appendInt(arg5)
                it.appendInt(count)
            }
        }
        register<Incoming.RoomReady> {
            read {
                Incoming.RoomReady(roomType = readString(), roomId = readInteger().toLong())
            }
            write {
                it.appendString(roomType)
                it.appendInt(roomId.toInt())
            }
        }
        register<Incoming.RoomProperty> {
            read {
                when (val key = readString()) {
                    "floor" -> Incoming.RoomProperty.Floor(value = readString().toInt())
                    "landscape" -> Incoming.RoomProperty.Landscape(value = readString().toDouble())
                    else -> throw Exception("Unsupported room property (key=$key)")
                }
            }
            write {
                when (this) {
                    is Incoming.RoomProperty.Floor -> it.appendString(value.toString())
                    is Incoming.RoomProperty.Landscape -> it.appendString(value.toString())
                }
            }
        }
        register<Incoming.RoomRating> {
            read {
                Incoming.RoomRating(rating = readInteger(), arg2 = readBoolean())
            }
            write {
                it.appendInt(rating)
                it.appendBoolean(arg2)
            }
        }
        register<Incoming.YouAreController> {
            read {
                Incoming.YouAreController(roomId = readInteger().toLong(), arg2 = readInteger())
            }
            write {
                it.appendInt(roomId.toInt())
                it.appendInt(arg2)
            }
        }
        register<Incoming.GetGuestRoomResult> {
            read {
                Incoming.GetGuestRoomResult(
                    enterRoom = readBoolean(),
                    id = readInteger(),
                    name = readString(),
                    ownerId = readInteger(),
                    ownerName = readString(),
                    doorMode = readInteger(),
                    userCount = readInteger(),
                    maxUserCount = readInteger(),
                    description = readString(),
                    tradeMode = readInteger(),
                    score = readInteger()
                )
            }
            write {
                it.appendBoolean(enterRoom)
                it.appendInt(id)
                it.appendString(name)
                it.appendInt(ownerId)
                it.appendString(ownerName)
                it.appendInt(doorMode)
                it.appendInt(userCount)
                it.appendInt(maxUserCount)
                it.appendString(description)
                it.appendInt(tradeMode)
                it.appendInt(score)
            }
        }
        register<Incoming.Users> {
            read {
                Incoming.Users(entities = readEntities())
            }
            write {
                it.appendInt(entities.size)
                entities.forEach { entity -> it.append(entity) }
            }
        }
        register<Incoming.UserRemove> {
            read { Incoming.UserRemove(userIndex = readString().toInt()) }
            write { it.appendString(userIndex.toString()) }
        }
        register<Incoming.Objects> {
            read {
                val count = readInteger()
                val ownerIdNameMap = buildMap<Int, String>(capacity = count) {
                    repeat(count) {
                        put(readInteger(), readString())
                    }
                }
                val floorItemList = List(size = readInteger()) {
                    readFloorItem(ownerNameProvider = ownerIdNameMap::get)
                }
                Incoming.Objects(floorItems = floorItemList)
            }
            write {
                val ownerIdMap = floorItems
                    .associateBy { item -> item.ownerId }
                    .mapValues { item -> item.value.ownerName }
                it.appendInt(ownerIdMap.size)
                for ((ownerId, ownerName) in ownerIdMap) {
                    it.appendInt(ownerId)
                    it.appendString(ownerName)
                }
                it.appendInt(floorItems.size)
                for (floorItem in floorItems)
                    it.append(floorItem)
            }
        }
        register<Incoming.ObjectAdd> {
            read { Incoming.ObjectAdd(floorItem = readFloorItem()) }
            write { it.append(floorItem) }
        }
        register<Incoming.ObjectRemove> {
            read {
                Incoming.ObjectRemove(
                    floorItemId = readInteger(),
                    arg2 = readBoolean(),
                    furniOwnerId = readInteger(),
                    arg4 = readInteger()
                )
            }
            write {
                it.appendInt(floorItemId)
                it.appendBoolean(arg2)
                it.appendInt(furniOwnerId)
                it.appendInt(arg4)
            }
        }
        register<Incoming.ObjectUpdate> {
            read { Incoming.ObjectUpdate(floorItem = readFloorItem()) }
            write { it.append(floorItem) }
        }
        register<Incoming.ObjectDataUpdate> {
            read {
                val floorItemId = readString().toInt()
                val category = readInteger()
                Incoming.ObjectDataUpdate(floorItemId, category, HStuff.readData(this, category).toList())
            }
            write {
                TODO("Not implemented, missing encoder for HStuff")
            }
        }
        register<Incoming.GetOwnOffersResult>(name = "MarketPlaceOwnOffers") {
            read { Incoming.GetOwnOffersResult(offers = Offers.Own.parse(this)) }
            write { TODO("Not implemented, missing encoder for Offers.Own") }
        }
        register<Incoming.GetOffersResult>(name = "MarketPlaceOffers") {
            read { Incoming.GetOffersResult(offers = Offers.All.parse(this)) }
            write { TODO("Not implemented, missing encoder for Offers.All") }
        }
        register<Incoming.MakeOfferResult>(name = "MarketplaceMakeOfferResult") {
            read {
                when (val responseCode = readInteger()) {
                    1 -> Incoming.MakeOfferResult.Success
                    2 -> Incoming.MakeOfferResult.Failure
                    else -> throw Exception("Unexpected response code $responseCode for `MarketplaceMakeOfferResult`.")
                }
            }
            write {
                it.appendInt(
                    when (this) {
                        Incoming.MakeOfferResult.Success -> 1
                        Incoming.MakeOfferResult.Failure -> 2
                    }
                )
            }
        }
        register<Incoming.FurniList> {
            read {
                readIndex = 14
                Incoming.FurniList(inventoryItems = List(size = readInteger()) {
                    readInventoryItem()
                })
            }
            write { TODO("Not implemented, missing encoder for FurniList") }
        }
        register<Incoming.FurniListRemove> {
            read {
                Incoming.FurniListRemove(furniUniqueId = readInteger())
            }
            write {
                it.appendInt(furniUniqueId)
            }
        }
        register<Incoming.FurniListAddOrUpdate> {
            read {
                Incoming.FurniListAddOrUpdate(
                    furniUniqueId = readInteger(),
                    furniProductType = ProductType.fromString(readString())!!,
                    furniUniqueIdUnsigned = readInteger(),
                    furniTypeId = readInteger(),
                    arg5 = readInteger(),
                    arg6 = readInteger(),
                    arg7 = readInteger(),
                    arg8 = readBoolean(),
                    arg9 = readBoolean(),
                    arg10 = readInteger(),
                    arg11 = readBoolean(),
                    arg12 = readInteger(),
                    arg13 = readInteger(),
                    arg14 = readString()
                )
            }
            write {
                it.appendInt(furniUniqueId)
                it.appendString(furniProductType.toString())
                it.appendInt(furniUniqueIdUnsigned)
                it.appendInt(furniTypeId)
                it.appendInt(arg5)
                it.appendInt(arg6)
                it.appendInt(arg7)
                it.appendBoolean(arg8)
                it.appendBoolean(arg9)
                it.appendInt(arg10)
                it.appendBoolean(arg11)
                it.appendInt(arg12)
                it.appendInt(arg13)
                it.appendString(arg14)
            }
        }
        register<Incoming.CreditBalance> {
            read { Incoming.CreditBalance(balance = readString().toDouble().toInt()) }
            write { it.appendString(balance.toDouble().toString()) }
        }
        register<Incoming.NavigatorSearchResults>(name = "NavigatorSearchResultBlocks") {
            read { Incoming.NavigatorSearchResults(result = readNavigatorSearchResult()) }
            write { it.append(result) }
        }
        register<Incoming.GetWardrobeResult>(name = "Wardrobe") {
            read {
                Incoming.GetWardrobeResult(
                    arg1 = readInteger(),
                    lookEntries = List(size = readInteger()) {
                        Incoming.GetWardrobeResult.Entry(
                            id = readInteger(),
                            look = readLook()
                        )
                    }
                )
            }
            write {
                it.appendInt(arg1)
                it.appendInt(lookEntries.size)
                for (entry in lookEntries) {
                    it.appendInt(entry.id)
                    it.append(entry.look)
                }
            }
        }
        register<Incoming.GetNFTWardrobeResult>(name = "UserNftWardrobe") {
            read { Incoming.GetNFTWardrobeResult(arg1 = readInteger()) }
            write { it.appendInt(arg1) }
        }
        register<Incoming.GetHotLooksResult>(name = "HotLooks") {
            read {
                Incoming.GetHotLooksResult(
                    looks = List(size = readInteger()) { readLook() }
                )
            }
            write {
                it.appendInt(looks.size)
                for (look in looks)
                    it.append(look)
            }
        }
        register<Incoming.FigureUpdate> {
            read {
                Incoming.FigureUpdate(
                    look = Look(
                        figureString = readString(),
                        gender = Gender.fromString(readString())!!
                    )
                )
            }
            write { it.append(look) }
        }
        register<Incoming.TradingOpen> {
            read {
                Incoming.TradingOpen(
                    myUserId = readInteger(),
                    arg2 = readInteger(),
                    otherUserId = readInteger(),
                    arg4 = readInteger()
                )
            }
            write {
                it.appendInt(myUserId)
                it.appendInt(arg2)
                it.appendInt(otherUserId)
                it.appendInt(arg4)
            }
        }
        register<Incoming.TradingAccept> {
            read {
                Incoming.TradingAccept(
                    userId = readInteger(),
                    userAction = readInteger()
                )
            }
            write {
                it.appendInt(userId)
                it.appendInt(userAction)
            }
        }
        register<Incoming.TradingConfirmation>()
        register<Incoming.TradingCompleted>()
        register<Incoming.TradingClose> {
            read {
                Incoming.TradingClose(
                    userId = readInteger(),
                    reason = readInteger()
                )
            }
            write {
                it.appendInt(userId)
                it.appendInt(reason)
            }
        }
    }
}
