package karth.plugin.entity

import karth.core.KarthSession
import karth.core.message.Message.Incoming
import tornadofx.booleanProperty
import tornadofx.getValue
import tornadofx.intProperty

class LiveAvatar(val session: KarthSession) {

    val tradingProperty = booleanProperty(false)
    val tradingUserIdProperty = intProperty(-1)

    val trading by tradingProperty
    val tradingUserId by tradingUserIdProperty

    init {
        session.apply {
            onEach<Incoming.TradingOpen> {
                tradingUserIdProperty.set(otherUserId)
                tradingProperty.set(true)
            }
            onEach<Incoming.TradingClose> {
                tradingUserIdProperty.set(-1)
                tradingProperty.set(false)
            }
        }
    }
}