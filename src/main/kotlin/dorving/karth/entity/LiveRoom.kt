package dorving.karth.entity

import dorving.karth.KarthSession
import dorving.karth.message.Message
import gearth.extensions.parsers.HFloorItem
import tornadofx.asObservable
import tornadofx.intProperty
import tornadofx.objectProperty
import tornadofx.stringProperty

class LiveRoom(private val session: KarthSession) {

    val roomIdProperty = intProperty()
    val roomNameProperty = stringProperty()

    val roomOwnerIdProperty = intProperty()
    val roomOwnerNameProperty = stringProperty()

    val roomRatingProperty = intProperty()

    var selectedFloorItemProperty = objectProperty<LiveFloorItem?>()

    val floorItemList = mutableListOf<LiveFloorItem>().asObservable()

    init {
        session.onEach<Message.Incoming.GetGuestRoomResult> {
            roomIdProperty.set(roomId)
            roomNameProperty.set(roomName)
            roomOwnerIdProperty.set(roomOwnerId)
            roomOwnerNameProperty.set(roomOwnerName)
            roomRatingProperty.set(roomRating)
        }
        session.onEach<Message.Incoming.Objects> {
            println("received ${floorItems.size} flooritems")
            floorItemList.setAll(floorItems.map {it.live()})
        }
        session.onEach<Message.Incoming.ObjectAdd> {
            floorItemList.add(floorItem.live())
        }
        session.onEach<Message.Incoming.ObjectRemove> {
            floorItemList.removeIf { it.id == floorItemId }
        }
        session.onEach<Message.Incoming.ObjectUpdate> {
            floorItemList.find { it.id == floorItem.id }?.update(this)
        }
        session.onEach<Message.Incoming.ObjectDataUpdate> {
            floorItemList.find { it.id == floorItemId }?.updateData(this)
        }
        session.onEach<Message.Outgoing.UseFurniture> {
            selectedFloorItemProperty.set(floorItemList.find { it.id == furniId })
        }
    }

    private fun HFloorItem.live() = when(typeId) {
        in 7652..7666 -> LiveNumericBlock(session, this)
        else -> LiveFloorItem(session, this)
    }
}
