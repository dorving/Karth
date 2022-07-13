package karth.plugin

import com.github.michaelbull.logging.InlineLogger
import gearth.extensions.ExtensionSession
import gearth.protocol.HPacket
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.MessageToByteEncoder

class ExtensionClient(
    private val host: String = "127.0.0.1",
    private val args: Array<String>,
    val plugin: Plugin<*>
) : Runnable {

    override fun run() {

        val extensionSession = ExtensionSession(args, plugin)
        val port = extensionSession.port

        logger.debug { "Starting extension client, connecting to `$host:$port`" }

        val group: EventLoopGroup = NioEventLoopGroup()
        val boostrap = Bootstrap()
            .group(group)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(Initializer(plugin, extensionSession))
        val future = boostrap.connect(host, port).sync()
        val channel = future.await()

        logger.info { "Established connection to G-Earth server (channel=$channel)" }
    }

    sealed class PacketDecodeStage {
        object Length : PacketDecodeStage()
        object Payload : PacketDecodeStage()
    }

    class Initializer(val plugin: Plugin<*>, private val extensionClient: ExtensionSession) : ChannelInitializer<SocketChannel>() {
        override fun initChannel(ch: SocketChannel) {
            logger.debug { "Initialize channel (channel=$ch)" }
            plugin.channel = ch
            ch.pipeline().apply {
                addLast("decoder", Decoder())
                addLast("encoder", Encoder())
                addLast("handler", Handler(extensionClient))
            }
        }
    }

    class Handler(private val client: ExtensionSession) : ChannelInboundHandlerAdapter() {

        override fun handlerAdded(ctx: ChannelHandlerContext) {
            logger.trace { "Channel registered (client=$client, channel=${ctx.channel()})" }
            super.handlerAdded(ctx)
        }

        override fun channelUnregistered(ctx: ChannelHandlerContext) {
            logger.trace { "Channel unregistered (client=$client, channel=${ctx.channel()})" }
            super.channelUnregistered(ctx)
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is ExtensionMessage.Incoming) {
                client.handleIncomingMessage(msg)
            } else {
                logger.error { "Invalid message type (message=$msg)" }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            ctx.exceptionCaught(cause)
        }
    }

    class Decoder : ByteToMessageDecoder() {

        private var stage: PacketDecodeStage = PacketDecodeStage.Length
        private var length: Int = 0

        override fun decode(ctx: ChannelHandlerContext?, buf: ByteBuf, out: MutableList<Any>) {
            when (stage) {
                PacketDecodeStage.Length -> {
                    if (buf.readableBytes() < Int.SIZE_BYTES)
                        return
                    length = buf.readInt()
                    stage = PacketDecodeStage.Payload
                }
                PacketDecodeStage.Payload -> {
                    if (buf.readableBytes() < length)
                        return
                    try{
                        val bytes = ByteArray(4 + length)
                        buf.readBytes(bytes, 4, length)
                        val hPacket = HPacket(bytes)
                        hPacket.fixLength()
                        val structure = ExtensionCodec.incomingMap[hPacket.headerId()]
                        if (structure != null) {
                            val reader = structure.read
                            if (reader != null) {
                                val message = reader.invoke(hPacket, null)
                                out.add(message)
                            } else
                                logger.error { "Did not find reader for packet $hPacket" }
                        } else
                            logger.error { "Did not find structure for packet $hPacket" }
                    } catch (e: Throwable) {
                        logger.error(e) { "Failed to decode payload (length=$length)" }
                    } finally {
                        length = 0
                        stage = PacketDecodeStage.Length
                    }
                }
            }
        }
    }

    class Encoder : MessageToByteEncoder<ExtensionMessage.Outgoing>() {
        override fun encode(ctx: ChannelHandlerContext, msg: ExtensionMessage.Outgoing, out: ByteBuf) {
            val structure = ExtensionCodec.outgoingMap[msg]
            if (structure == null) {
                logger.error { "Structure for packet not defined (packet=$msg)" }
                return
            }
            val packet = HPacket(structure.headerId)
            structure.write(msg, packet)
            out.writeBytes(packet.toBytes())
            logger.info { "Written packet (structure=${structure.name}, packet=$packet)" }
        }
    }
}

private val logger = InlineLogger()

private fun ChannelHandlerContext.exceptionCaught(cause: Throwable) {
    logger.error(cause) { "Channel exception caught (channel=${channel()})" }
    channel().close()
}
