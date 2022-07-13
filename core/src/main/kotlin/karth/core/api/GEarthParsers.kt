package karth.core.api

import gearth.extensions.parsers.HDirection
import gearth.extensions.parsers.HPoint

fun HPoint.toKarth() = Point(x, y, z)
fun HDirection.toKarth() = Direction.valueOf(name)