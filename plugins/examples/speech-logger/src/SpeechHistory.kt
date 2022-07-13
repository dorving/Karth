import com.github.michaelbull.logging.InlineLogger
import karth.core.message.Message
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val logger = InlineLogger(SpeechHistory::class)

@kotlinx.serialization.Serializable
data class SpeechHistory(
    val chats: MutableList<SpeechEntry<Message.Incoming.Chat>> = mutableListOf(),
    val shouts: MutableList<SpeechEntry<Message.Incoming.Shout>> = mutableListOf(),
    val whispers: MutableList<SpeechEntry<Message.Incoming.Whisper>> = mutableListOf(),
) {
    fun<T : Message.Incoming.Speech> add(communication: T) {
        val timeStamp = System.currentTimeMillis()
        when(communication) {
            is Message.Incoming.Chat -> chats += SpeechEntry(timeStamp, communication)
            is Message.Incoming.Shout -> shouts += SpeechEntry(timeStamp, communication)
            is Message.Incoming.Whisper -> whispers += SpeechEntry(timeStamp, communication)
            else -> error("Unsupported communication type (communication=$communication)")
        }
    }
}

@kotlinx.serialization.Serializable
data class SpeechEntry<T : Message.Incoming.Speech>(
    val timeStamp: Long,
    val speech: T,
)

internal fun saveSpeechHistory(file: File, history: SpeechHistory) {
    file.outputStream().use {
        Json.encodeToStream(history, it)
    }
}

internal fun readSpeechHistory(file: File) : SpeechHistory {
    if (!file.exists())
        return SpeechHistory()
    return try {
        file.inputStream().use(Json::decodeFromStream)
    } catch (e: Exception) {
        val backupFile = file.parentFile.resolve(file.nameWithoutExtension+"_BACKUP"+file.extension)
        logger.error(e) {
            "Failed to load message history from file $file, " +
                    "loading empty history instead," +
                    "copied backup to $backupFile "
        }
        try {
            file.copyTo(backupFile)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to make a backup of $file at $backupFile"  }
        }
        SpeechHistory()
    }
}

internal fun getSpeechHistoryFileOfCurrentDateFor(folderName: String): File {
    val outFile = dataPath
        .resolve(folderName)
        .resolve("${getDate()}.json").toFile()
    if (!outFile.parentFile.exists())
        outFile.parentFile.mkdirs()
    return outFile
}

private fun getDate() = SimpleDateFormat("yyyy-MM-dd").format(Date())