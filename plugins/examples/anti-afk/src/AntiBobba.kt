
import gearth.extensions.launch
import karth.plugin.Plugin
import karth.plugin.PluginInfo
import karth.core.message.Message.Outgoing
import karth.core.protocol.PacketStructureCodecFactory
import karth.protocol.habbo.HabboCodec

@PluginInfo(
    title = "AntiBobba",
    author = "Dorving",
    description = "Obfuscates messages",
    version = "1.0",
    fxmlFile = "anti-bobba.fxml",
    iconFile = "anti-bobba.png"
)
class AntiBobbaPlugin : Plugin<Controller>() {

    override val codecs: PacketStructureCodecFactory = HabboCodec
    override fun onStartConnection() {
        onEach<Outgoing.Speech> {
            block()
            send(obfuscate())
        }
    }
    private fun Outgoing.Speech.obfuscate() = when (this) {
        is Outgoing.Chat -> Outgoing.Chat(contents.obfuscate(), bubble, messageCount)
        is Outgoing.Shout -> Outgoing.Shout(contents.obfuscate(), bubble)
        is Outgoing.Whisper -> Outgoing.Whisper(contents.obfuscate(), bubble)
        else -> error("Unsupported speech type $this")
    }
    private fun String.obfuscate(): String = split(" ").joinToString(" ") {
        if (it.startsWith(":") || ignoreWords.contains(it.lowercase()))
            it
        else
            it.toCharArray().joinToString(separator = INVISIBLE_CHAR, postfix = INVISIBLE_CHAR)
    }
    companion object {
        private const val INVISIBLE_CHAR = "ҖҖ"
        private val ignoreWords = setOf("the", "a", "i", "would", "you", "alright", "okay", "ok", "exit")
    }
}
class Controller
fun main() = launch<AntiBobbaPlugin>("-p", "9092")
