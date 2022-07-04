package dorving.karth.entity

import dorving.karth.KarthSession
import dorving.karth.message.Message
import gearth.extensions.parsers.HDirection
import gearth.extensions.parsers.HFloorItem
import gearth.extensions.parsers.HPoint
import tornadofx.*

open class LiveFloorItem(protected val session: KarthSession, floorItem: HFloorItem) {

    val id by intProperty(floorItem.id)
    val category by intProperty(floorItem.category)
    var facing by objectProperty(floorItem.facing)
    var tile by objectProperty(floorItem.tile)

    val x by floorItem.tile::x
    val y by floorItem.tile::y
    val z by floorItem.tile::z

    val stuffList = observableListOf(*(floorItem.stuff?: emptyArray()))

    private val moveListeners = mutableListOf<MoveListener>()
    private val rotateListeners = mutableListOf<RotateListener>()

    fun updateData(objectDataUpdate: Message.Incoming.ObjectDataUpdate) {
        stuffList.setAll(*objectDataUpdate.data)
    }

    fun update(objectUpdate: Message.Incoming.ObjectUpdate) {
        val floorItem = objectUpdate.floorItem

        val oldTile = tile
        val newTile = floorItem.tile
        tile = newTile
        moveListeners.forEach { it.onMove(oldTile, newTile) }

        val oldFacing = facing
        val newFacing = floorItem.facing
        facing = newFacing
        rotateListeners.forEach { it.onRotate(oldFacing, newFacing) }
    }

    fun onMove(moveListener: MoveListener) {
        moveListeners += moveListener
    }

    fun onRotate(rotateListener: RotateListener) {
        rotateListeners += rotateListener
    }

    fun interface MoveListener {
        fun onMove(old: HPoint, new: HPoint)
    }

    fun interface RotateListener {
        fun onRotate(old: HDirection, new: HDirection)
    }
}
