package gearth.extensions

import com.github.michaelbull.logging.InlineLogger
import gearth.protocol.HMessage
import gearth.protocol.connection.HClient
import gearth.services.Constants
import karth.core.protocol.ClientPacket
import karth.plugin.ExtensionMessage
import karth.plugin.ExtensionMessage.Outgoing
import karth.plugin.Plugin
import tornadofx.findMethodByName
import java.util.*

class ExtensionSession(private val args: Array<String>, val plugin: Plugin<*>)  {

    private val extension: Extension by lazy {
        val extensionField = ExtensionForm::class.java.getDeclaredField("extension")
        extensionField.trySetAccessible()
        extensionField.get(plugin) as Extension
    }

    private val logger by lazy { InlineLogger(this::class) }

    @Volatile
    private var delayInit = false

    val port = getArgument(args, *PORT_FLAG)!!.toInt()

    fun handleIncomingMessage(message: ClientPacket) {
        logger.info { message }
        when (message) {
            is ExtensionMessage.Incoming.InfoRequest -> {
                val pluginInfo = plugin.getInfo()
                send(
                    Outgoing.ExtensionInfo(
                        title = pluginInfo.title,
                        author = pluginInfo.author,
                        version = pluginInfo.version,
                        description = pluginInfo.description,
                        onClickUsed = plugin::class.java.findMethodByName("onClick") != null,
                        file = getArgument(args, *FILE_FLAG),
                        cookie = getArgument(args, *COOKIE_FLAG),
                        canLeave = plugin.canLeave(),
                        canDelete = plugin.canDelete()
                    ),
                )
            }
            is ExtensionMessage.Incoming.ConnectionStart -> {
                Constants.UNITY_PACKETS = message.clientType == HClient.UNITY
                extension.packetInfoManager = message.packetInfoManager
                if (delayInit) {
                    plugin.initExtension()
                    delayInit = false
                }
                plugin.session.connect(message.clientType)
                plugin.onStartConnection()
            }
            is ExtensionMessage.Incoming.ConnectionEnd -> plugin.onEndConnection()
            is ExtensionMessage.Incoming.FlagsCheck -> {
                // plugin.flagRequestCallback.act(message.flags
            }
            is ExtensionMessage.Incoming.Init -> {
                delayInit = message.delayInit
                // TODO: updateHostInfo
                // plugin.updateHostInfo(message.hostInfo)
                if (!delayInit)
                    plugin.initExtension()
            }
            is ExtensionMessage.Incoming.OnDoubleClick -> {
                plugin.onClick()
            }
            is ExtensionMessage.Incoming.PacketIntercept -> {
                val interceptedMessage = HMessage(message.packetString)
                plugin.useQueue = true
                try {
                    extension.modifyMessage(interceptedMessage)
                    send(Outgoing.ManipulatedPacket(interceptedMessage))
                    plugin.writeAndFLushPendingMessages()
                } catch (e: Exception) {
                    logger.error(e) { "Failed to write manipulated packet, clearing any pending outgoing messages" }
                    plugin.clearPendingMessages()
                } finally {
                    plugin.useQueue = false
                }
            }
            is ExtensionMessage.Incoming.UpdateHostInfo -> {
                // TODO: updateHostInfo
                // plugin.updateHostInfo(message.hostInfo)
            }
        }
    }

    private fun writeToConsole(colorClass: String, s: String, mentionTitle: Boolean) {
        val pluginInfo = plugin.getInfo()
        val text = "[" + colorClass + "]" + (if (mentionTitle) pluginInfo.title + " --> " else "") + s
        send(Outgoing.ExtensionConsoleLog(text), )
    }

    private fun send(outgoing: Outgoing) {
        plugin.channel.writeAndFlush(outgoing)
    }

    companion object {

        private val PORT_FLAG = arrayOf("--port", "-p")
        private val FILE_FLAG = arrayOf("--filename", "-f")
        private val COOKIE_FLAG = arrayOf("--auth-token", "-c") // don't add a cookie or filename when debugging

        private fun getArgument(args: Array<String>, vararg arg: String): String? {
            for (i in 0 until args.size - 1)
                for (str in arg)
                    if (args[i].lowercase(Locale.getDefault()) == str.lowercase(Locale.getDefault()))
                        return args[i + 1]
            return null
        }
    }
}
