package karth.protocol.habbo

import gearth.extensions.parsers.HStuff
import gearth.protocol.HPacket
import karth.core.api.*

fun HPacket.append(look: Look) {
    appendString(look.figureString)
    appendString(look.gender.toString())
}

fun HPacket.readLook() =
    Look(figureString = readString(), gender = Gender.fromString(readString())!!)

fun HPacket.append(stuff: Stuff): Unit =
    TODO("Missing encoder for Stuff (stuff=$stuff)")

fun HPacket.append(point: Point) {
    appendInt(point.x)
    appendInt(point.y)
    appendString(point.toString())
}
fun HPacket.readPoint() =
    Point(readInteger(), readInteger(), readString().toDouble())

fun HPacket.append(gender: Gender) {
    appendString(gender.id)
}

fun HPacket.readGender() =
    Gender.fromString(readString())!!


fun HPacket.readEntities() =
    List(readInteger()) { readEntity() }

fun HPacket.append(entity: Entity) {
    appendInt(entity.id)
    appendString(entity.name)
    appendString(entity.motto)
    appendString(entity.figureId)
    appendInt(entity.index)
    append(entity.tile)
    appendInt(entity.unknown1)
    appendInt(entity.entityType.id)
    when(entity) {
        is Entity.Habbo -> {
            append(entity.gender)
            appendInt(entity.arg1)
            appendInt(entity.arg2)
            appendString(entity.favoriteGroup)
            appendString(entity.arg4)
            appendInt(entity.arg5)
            appendBoolean(entity.arg6)
        }
        is Entity.Pet -> {
            appendInt(entity.arg0)
            appendInt(entity.arg1)
            appendString(entity.arg2)
            appendInt(entity.arg3)
            appendBoolean(entity.arg4)
            appendBoolean(entity.arg5)
            appendBoolean(entity.arg6)
            appendBoolean(entity.arg7)
            appendBoolean(entity.arg8)
            appendBoolean(entity.arg9)
            appendInt(entity.arg10)
            appendString(entity.arg11)
        }
        is Entity.LegacyBot -> {}
        is Entity.Bot -> {
            appendString(entity.arg0)
            appendInt(entity.arg1)
            appendString(entity.arg2)
            appendInt(entity.arg4.size)
            entity.arg4.forEach { appendShort(it) }
        }
    }
}
fun HPacket.readEntity(): Entity {
    val id = readInteger()
    val name = readString()
    val motto = readString()
    val figureId = readString()
    val index = readInteger()
    val tile = readPoint()
    val unknown1 = readInteger()
    return when (EntityType.forId(readInteger())!!) {
        EntityType.HABBO -> Entity.Habbo(
            id = id,
            name = name,
            motto = motto,
            figureId = figureId,
            index = index,
            tile = tile,
            unknown1 = unknown1,
            gender = readGender(),
            arg1 = readInteger(),
            arg2 = readInteger(),
            favoriteGroup = readString(),
            arg4 = readString(),
            arg5 = readInteger(),
            arg6 = readBoolean()
        )
        EntityType.PET -> Entity.Pet(
            id = id,
            name = name,
            motto = motto,
            figureId = figureId,
            index = index,
            tile = tile,
            unknown1 = unknown1,
            arg0 = readInteger(),
            arg1 = readInteger(),
            arg2 = readString(),
            arg3 = readInteger(),
            arg4 = readBoolean(),
            arg5 = readBoolean(),
            arg6 = readBoolean(),
            arg7 = readBoolean(),
            arg8 = readBoolean(),
            arg9 = readBoolean(),
            arg10 = readInteger(),
            arg11 = readString()
        )
        EntityType.LEGACY_BOT -> Entity.LegacyBot(
            id = id,
            name = name,
            motto = motto,
            figureId = figureId,
            index = index,
            tile = tile,
            unknown1 = unknown1
        )
        EntityType.BOT -> Entity.Bot(
            id = id,
            name = name,
            motto = motto,
            figureId = figureId,
            index = index,
            tile = tile,
            unknown1 = unknown1,
            arg0 = readString(),
            arg1 = readInteger(),
            arg2 = readString(),
            arg4 = List(readInteger()) { readShort() }
        )
    }
}


fun HPacket.append(inventoryItem: InventoryItem) {
    appendInt(inventoryItem.placementId)
    appendString(inventoryItem.type.id)
    appendInt(inventoryItem.id)
    appendInt(inventoryItem.typeId)
    appendInt(inventoryItem.specialType.id)
    appendInt(inventoryItem.category)
    append(inventoryItem.stuff)
    appendBoolean(inventoryItem.recyclable)
    appendBoolean(inventoryItem.tradeable)
    appendBoolean(inventoryItem.groupable)
    appendBoolean(inventoryItem.sellable)
    appendInt(inventoryItem.secondsToExpiration)
    appendBoolean(inventoryItem.rentPeriodStarted)
    appendInt(inventoryItem.roomId)
    if (inventoryItem.type == ProductType.FloorItem) {
        appendString(inventoryItem.slotId)
        appendInt(inventoryItem.extra)
    }
}

fun HPacket.readInventoryItem(): InventoryItem {
    val placementId = readInteger()
    val type = ProductType.fromString(readString())!!
    val id = readInteger()
    val typeId = readInteger()
    val specialType = SpecialType.fromId(readInteger())!!
    val category = readInteger()
    val stuff = HStuff.readData(this, category).toList()
    val recyclable = readBoolean()
    val tradeable = readBoolean()
    val groupable = readBoolean()
    val sellable = readBoolean()
    val secondsToExpiration = readInteger()
    val rentPeriodStarted = readBoolean()
    val roomId = readInteger()
    val (slotId, extra) = if (type == ProductType.FloorItem)
        readString() to readInteger()
    else
        "" to -1
    return InventoryItem(
        placementId = placementId,
        type = type,
        id = id,
        typeId = typeId,
        specialType = specialType,
        category = category,
        stuff = stuff,
        recyclable = recyclable,
        tradeable = tradeable,
        groupable = groupable,
        sellable = sellable,
        secondsToExpiration = secondsToExpiration,
        rentPeriodStarted = rentPeriodStarted,
        roomId = roomId,
        slotId = slotId,
        extra = extra
    )
}

fun HPacket.append(floorItem: FloorItem) {
    appendInt(floorItem.id)
    appendInt(floorItem.typeId)
    appendInt(floorItem.x)
    appendInt(floorItem.y)
    appendInt(floorItem.facing.ordinal)
    appendString(floorItem.z.toString())
    appendString(floorItem.ignore1)
    appendInt(floorItem.ignore2)
    appendInt(floorItem.category)
    append(floorItem.stuff)
    appendInt(floorItem.secondsToExpiration)
    appendInt(floorItem.usagePolicy)
    appendInt(floorItem.ownerId)
    if (floorItem.typeId < 0)
        appendString(floorItem.ignore3!!)
}

fun HPacket.readFloorItem(ownerNameProvider: (Int) -> String? = { null }): FloorItem {
    val id = readInteger()
    val typeId = readInteger()
    val x: Int = readInteger()
    val y: Int = readInteger()
    val facing = Direction.values()[readInteger()]
    val tile = Point(x, y, readString().toDouble())
    val ignore1 = readString()
    val ignore2 = readInteger()
    val category = readInteger()
    val stuff = HStuff.readData(this, category).toList()
    val secondsToExpiration = readInteger()
    val usagePolicy = readInteger()
    val ownerId = readInteger()
    val ignore3 = if (typeId < 0) readString() else null
    return FloorItem(
        id = id,
        typeId = typeId,
        tile = tile,
        facing = facing,
        category = category,
        secondsToExpiration = secondsToExpiration,
        usagePolicy = usagePolicy,
        ownerId = ownerId,
        ownerName = ownerNameProvider(ownerId),
        stuff = stuff,
        ignore1 = ignore1,
        ignore2 = ignore2,
        ignore3 = ignore3
    )
}

fun HPacket.append(searchResult: NavigatorSearchResult) {
    appendString(searchResult.searchCode)
    appendString(searchResult.filteringData)
    appendInt(searchResult.blocks.size)
    for (block in searchResult.blocks) {
        appendString(block.searchCode)
        appendString(block.text)
        appendInt(block.actionAllowed)
        appendBoolean(block.isForceClosed)
        appendInt(block.viewMode)
        appendInt(block.rooms.size)
        for (room in block.rooms) {
            appendInt(room.flatId)
            appendString(room.roomName)
            appendInt(room.ownerId)
            appendString(room.ownerName)
            appendInt(room.doorMode)
            appendInt(room.userCount)
            appendInt(room.maxUserCount)
            appendString(room.description)
            appendInt(room.tradeMode)
            appendInt(room.score)
            appendInt(room.ranking)
            appendInt(room.categoryId)
            appendInt(room.tags.size)
            room.tags.forEach { appendString(it) }
            var multiUse = 0
            val objectsToAppend: MutableList<Any?> = ArrayList()

            val addOfficialRoomPickRef = room.officialRoomPicRef != null
            if (addOfficialRoomPickRef)
                multiUse = multiUse or 1

            val addGroupInfo = room.groupId != -1 && room.groupName != null && room.groupBadgeCode != null
            if (addGroupInfo)
                multiUse = multiUse or 2

            val addRoomAdInfo =
                room.roomAdName != null && room.roomAdDescription != null && room.roomAdExpiresInMin != -1
            if (addRoomAdInfo)
                multiUse = multiUse or 4

            if (room.showOwner)
                multiUse = multiUse or 8

            if (room.allowPets)
                multiUse = multiUse or 16

            if (room.displayRoomEntryAd)
                multiUse = multiUse or 32

            appendInt(multiUse)

            if (addOfficialRoomPickRef)
                objectsToAppend.add(room.officialRoomPicRef)

            if (addGroupInfo) {
                objectsToAppend.add(room.groupId)
                objectsToAppend.add(room.groupName)
                objectsToAppend.add(room.groupBadgeCode)
            }

            if (addRoomAdInfo) {
                objectsToAppend.add(room.roomAdName)
                objectsToAppend.add(room.roomAdDescription)
                objectsToAppend.add(room.roomAdExpiresInMin)
            }
        }
    }
}

fun HPacket.readNavigatorSearchResult() =
    NavigatorSearchResult(
        searchCode = readString(),
        filteringData = readString(),
        blocks = List(readInteger()) {
            NavigatorBlock(
                searchCode = readString(),
                text = readString(),
                actionAllowed = readInteger(),
                isForceClosed = readBoolean(),
                viewMode = readInteger(),
                rooms = List(readInteger()) {
                    val flatId = readInteger()
                    val roomName = readString()
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
                    val tags = Array(readInteger()) {
                        readString()
                    }
                    val multiUse = readInteger()
                    val officialRoomPicRef = if (multiUse and 1 > 0) readString() else null
                    val (groupId, groupName, groupBadgeCode) = if (multiUse and 2 > 0)
                        Triple(readInteger(), readString(), readString())
                    else
                        Triple(-1, null, null)
                    val (roomAdName, roomAdDescription, roomAdExpiresInMin) = if (multiUse and 2 > 0)
                        Triple(readString(), readString(), readInteger())
                    else
                        Triple(null, null, -1)
                    val showOwner = (multiUse and 8 > 0)
                    val allowPets = (multiUse and 16 > 0)
                    val displayRoomEntryAd = (multiUse and 32 > 0)
                    NavigatorRoom(
                        flatId = flatId,
                        roomName = roomName,
                        ownerId = ownerId,
                        ownerName = ownerName,
                        doorMode = doorMode,
                        userCount = userCount,
                        maxUserCount = maxUserCount,
                        description = description,
                        tradeMode = tradeMode,
                        score = score,
                        ranking = ranking,
                        categoryId = categoryId,
                        tags = tags,
                        officialRoomPicRef = officialRoomPicRef,
                        groupId = groupId,
                        groupName = groupName,
                        groupBadgeCode = groupBadgeCode,
                        roomAdName = roomAdName,
                        roomAdDescription = roomAdDescription,
                        roomAdExpiresInMin = roomAdExpiresInMin,
                        showOwner = showOwner,
                        allowPets = allowPets,
                        displayRoomEntryAd = displayRoomEntryAd
                    )
                }
            )
        }
    )