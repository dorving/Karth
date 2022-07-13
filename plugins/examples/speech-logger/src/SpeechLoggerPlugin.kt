
import com.github.michaelbull.logging.InlineLogger
import gearth.extensions.launch
import karth.plugin.Plugin
import karth.plugin.PluginInfo
import karth.plugin.util.liveRoom
import karth.core.message.Message.Incoming
import karth.core.protocol.PacketStructureCodecFactory
import karth.protocol.habbo.HabboCodec
import java.nio.file.Path
import java.nio.file.Paths

val dataPath: Path = Paths.get("data")

@PluginInfo(
    title = "Message Logger",
    author = "Dorving",
    description = "Logs messages",
    version = "1.0",
    fxmlFile = "message-logger.fxml",
    iconFile = "message-logger.png"
)
class SpeechLoggerPlugin : Plugin<Controller>() {

    private val logger = InlineLogger(SpeechLoggerPlugin::class)

    override val codecs: PacketStructureCodecFactory = HabboCodec

    override fun onStartConnection() {

        val liveRoom = liveRoom()

        onEach<Incoming.Speech> {
            if (liveRoom.isLoaded()) {
                val speechHistoryFile = getSpeechHistoryFileOfCurrentDateFor(liveRoom.id.toString())
                val speechHistory = readSpeechHistory(speechHistoryFile)
                speechHistory.add(this)
                saveSpeechHistory(speechHistoryFile, speechHistory)
            } else
                logger.warn { "No room is loaded, please (re-enter) room, ignoring $this" }
        }
    }
}

class Controller

fun main() = launch<SpeechLoggerPlugin>("-p", "9092")