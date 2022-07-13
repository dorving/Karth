package karth.plugin.util

import karth.plugin.Plugin
import karth.plugin.entity.LiveMarketPlace
import karth.plugin.entity.LiveRoom
import karth.plugin.entity.LiveWardrobe

fun Plugin<*>.liveMarketPlace() = LiveMarketPlace(session)

fun Plugin<*>.liveWardrobe() = LiveWardrobe(session)

fun Plugin<*>.liveRoom() = LiveRoom(session)
