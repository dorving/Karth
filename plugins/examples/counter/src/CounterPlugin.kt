import gearth.extensions.launch
import javafx.fxml.Initializable
import karth.plugin.Plugin
import karth.plugin.PluginInfo
import karth.plugin.entity.LiveNumericBlock
import karth.plugin.entity.LiveRoom
import karth.plugin.util.liveRoom
import karth.core.protocol.PacketStructureCodecFactory
import karth.protocol.habbo.HabboCodec
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

@PluginInfo(
    title = "Counter",
    author = "Dorving",
    description = "Update BC number blocks to specific state",
    version = "1.0",
    fxmlFile = "counter.fxml",
    iconFile = "counter.png"
)
class CounterPlugin : Plugin<Controller>() {

    override val codecs: PacketStructureCodecFactory = HabboCodec

    override fun onStartConnection() {
        val room = liveRoom()
        thread(start = true) {
            for (i in 0 until 9) {
                room.updateAllNumberBlocks(newValue = i)
                Thread.sleep(1000L)
            }
        }
    }

    private fun LiveRoom.updateAllNumberBlocks(newValue: Int) =
        floorItemList
            .filterIsInstance<LiveNumericBlock>()
            .forEach{ it.setValue(newValue) }
}

class Controller : Initializable {
    override fun initialize(location: URL?, resources: ResourceBundle?) {}
}

fun main() = launch<CounterPlugin>("-p", "9092")
