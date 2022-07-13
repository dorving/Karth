
import gearth.extensions.launch
import karth.plugin.Plugin
import karth.plugin.PluginInfo
import karth.core.message.Message.Incoming
import karth.core.message.Message.Outgoing
import karth.core.protocol.PacketStructureCodecFactory
import karth.protocol.habbo.HabboCodec
import kotlin.time.Duration.Companion.milliseconds

@PluginInfo(
    title = "Ping Pong",
    author = "Dorving",
    description = "Sends pong when receiving ping",
    version = "1.0",
    fxmlFile = "ping-pong.fxml",
    iconFile = "ping-pong.png"
)
class PingPongPlugin : Plugin<Controller>() {

    override val codecs: PacketStructureCodecFactory = HabboCodec
    override fun onStartConnection() {
        onEach<Incoming.Chat>(condition = { contents == "ping"}) {
            sendWithDelay(Outgoing.Chat("pong"), delayBy = 600.milliseconds)
        }
    }
}

class Controller

fun main() = launch<PingPongPlugin>("-p", "9092")
