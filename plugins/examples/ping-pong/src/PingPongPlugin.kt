
import gearth.extensions.launch
import karth.core.message.Message.Incoming
import karth.core.protocol.PacketStructureCodecFactory
import karth.plugin.Plugin
import karth.plugin.PluginInfo
import karth.protocol.habbo.HabboCodec

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
            send(Incoming.Chat(userIndex,"pong", arg3, bubble, arg5, count+1))
        }
    }
}

class Controller

fun main() = launch<PingPongPlugin>("-p", "9092")
