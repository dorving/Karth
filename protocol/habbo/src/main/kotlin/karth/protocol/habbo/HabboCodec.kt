package karth.protocol.habbo

import karth.core.protocol.PacketStructureCodecFactory

object HabboCodec : PacketStructureCodecFactory() {

    init {
        registerFlash(this)
        registerUnity(this)
    }
}
