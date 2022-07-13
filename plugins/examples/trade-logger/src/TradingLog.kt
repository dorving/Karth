import karth.core.message.Message

data class TradingLog(val tradingUserId: Int,
                      val receivedItem: List<Message.Incoming.FurniListAddOrUpdate>,
                      val removedItems: List<Message.Incoming.FurniListRemove>
)