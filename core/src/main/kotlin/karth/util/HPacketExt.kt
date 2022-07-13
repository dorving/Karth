package karth.util

import gearth.protocol.HPacket

fun HPacket.appendStringUTF8(s: String) = appendString(s, Charsets.UTF_8)
fun HPacket.readStringUTF8() = readString(Charsets.UTF_8)
fun HPacket.availableBytes() = bytesLength - readIndex