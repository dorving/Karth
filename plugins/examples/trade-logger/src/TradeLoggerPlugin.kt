
import com.github.michaelbull.logging.InlineLogger
import gearth.extensions.launch
import karth.core.message.Message.Incoming
import karth.core.protocol.PacketStructureCodecFactory
import karth.plugin.Plugin
import karth.plugin.PluginInfo
import karth.protocol.habbo.HabboCodec
import tornadofx.booleanProperty
import tornadofx.intProperty

@PluginInfo(
    title = "Trade Logger",
    author = "Dorving",
    description = "Logs trades",
    version = "1.0",
    fxmlFile = "message-logger.fxml",
    iconFile = "message-logger.png"
)
class TradeLoggerPlugin : Plugin<Controller>() {

    private val logger = InlineLogger(TradeLoggerPlugin::class)

    override val codecs: PacketStructureCodecFactory = HabboCodec

    private val tradingProperty = booleanProperty(false)
    private val tradingUserIdProperty = intProperty(-1)
    private val receivedItem = mutableListOf<Incoming.FurniListAddOrUpdate>()
    private val removedItems = mutableListOf<Incoming.FurniListRemove>()

    override fun onStartConnection() {
        onEach<Incoming.TradingOpen> {
            tradingUserIdProperty.set(otherUserId)
            tradingProperty.set(true)
        }
        onEach<Incoming.FurniListAddOrUpdate> {
            if (tradingProperty.get())
                receivedItem.add(this)
        }
        onEach<Incoming.FurniListRemove> {
            if (tradingProperty.get())
                removedItems.add(this)
        }
        onEach<Incoming.TradingCompleted> {
            logger.info {
                TradingLog(
                    tradingUserId = tradingUserIdProperty.get(),
                    receivedItem = receivedItem,
                    removedItems = removedItems
                )
            }
        }
        onEach<Incoming.TradingClose> {
            receivedItem.clear()
            removedItems.clear()
            tradingProperty.set(false)
            tradingUserIdProperty.set(-1)
        }
    }
}

class Controller

fun main() = launch<TradeLoggerPlugin>("-p", "9092")