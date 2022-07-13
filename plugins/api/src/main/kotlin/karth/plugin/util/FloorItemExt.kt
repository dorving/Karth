package karth.plugin.util

import karth.plugin.entity.LiveFloorItem
import karth.plugin.entity.LiveNumericBlock
import karth.core.KarthSession
import karth.core.api.FloorItem

fun FloorItem.live(session: KarthSession) = when(typeId) {
    in 7652..7666 -> LiveNumericBlock(session, this)
    else -> LiveFloorItem(session, this)
}
