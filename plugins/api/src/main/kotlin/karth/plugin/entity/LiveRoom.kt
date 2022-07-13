package karth.plugin.entity
import karth.plugin.util.live
import karth.core.KarthSession
import karth.core.message.Message
import tornadofx.*

class LiveRoom(private val session: KarthSession) {

    val roomIdProperty = intProperty()
    val roomNameProperty = stringProperty()

    val roomOwnerIdProperty = intProperty()
    val roomOwnerNameProperty = stringProperty()

    val roomRatingProperty = intProperty()

    var selectedFloorItemProperty = objectProperty<LiveFloorItem?>()

    val floorItemList = mutableListOf<LiveFloorItem>().asObservable()

    val id by roomIdProperty
    val name by roomNameProperty

    init {
        session.onEach<Message.Incoming.GetGuestRoomResult> {
            roomIdProperty.set(id)
            roomNameProperty.set(name)
            roomOwnerIdProperty.set(ownerId)
            roomOwnerNameProperty.set(ownerName)
            roomRatingProperty.set(score)
        }
        session.onEach<Message.Incoming.Objects> {
            floorItemList.setAll(floorItems.map {it.live(session)})
        }
        session.onEach<Message.Incoming.ObjectAdd> {
            floorItemList.add(floorItem.live(session))
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

    fun isLoaded(): Boolean = id != 0
}
