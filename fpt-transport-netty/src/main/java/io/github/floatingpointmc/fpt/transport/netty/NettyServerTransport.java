package io.github.floatingpointmc.fpt.transport.netty;

import io.github.floatingpointmc.fpt.codec.VarInt;
import io.github.floatingpointmc.fpt.protocol.*;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.MessageListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NettyServerTransport {

    private static final Logger LOGGER = Logger.getLogger(NettyServerTransport.class.getName());

    private final Protocol protocol;
    private final EventGroup eventGroup;
    private final MessageListener messageListener;
    private final boolean ownedEventGroup;

    private Channel serverChannel;

    public NettyServerTransport(@NotNull Protocol protocol, @NotNull EventGroup eventGroup, boolean ownedEventGroup, MessageListener messageListener) {
        this.protocol = protocol;
        this.eventGroup = eventGroup;
        this.ownedEventGroup = ownedEventGroup;
        this.messageListener = messageListener;
    }

    public int start(@NotNull String host, int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(eventGroup.bossGroup(), eventGroup.workerGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("frame-decoder", new NettyFrameDecoder(ProtocolConstants.MAX_PACKET_SIZE));
                        pipeline.addLast("handshake-handler", new ServerHandshakeHandler(protocol));
                        pipeline.addLast("c2s-decoder", new NettyMessageDecoder(protocol, MessageDirection.C2S));
                        pipeline.addLast("s2c-encoder", new NettyMessageEncoder(protocol));
                        pipeline.addLast("handler", new ServerChannelHandler(protocol, messageListener));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(host, port).sync();
        serverChannel = future.channel();
        int actualPort = ((java.net.InetSocketAddress) serverChannel.localAddress()).getPort();
        LOGGER.info("FPT Server started on " + host + ":" + actualPort);
        return actualPort;
    }

    public void stop() {
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
        }
        if (ownedEventGroup) {
            eventGroup.close();
        }
        LOGGER.info("FPT Server stopped");
    }

    public int getActualPort() {
        if (serverChannel != null) {
            return ((java.net.InetSocketAddress) serverChannel.localAddress()).getPort();
        }
        return -1;
    }

    static final class ServerHandshakeHandler extends ChannelInboundHandlerAdapter {
        private final Protocol protocol;
        private boolean handshakeDone = false;

        ServerHandshakeHandler(Protocol protocol) {
            this.protocol = protocol;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (handshakeDone) {
                ctx.fireChannelRead(msg);
                return;
            }
            if (msg instanceof io.netty.buffer.ByteBuf) {
                io.netty.buffer.ByteBuf buf = (io.netty.buffer.ByteBuf) msg;
                try {
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    ByteBuffer nioBuf = ByteBuffer.wrap(bytes);

                    HandshakeMessage handshake = HandshakeCodec.decode(nioBuf);

                    if (!handshake.matches(protocol)) {
                        LOGGER.warning("Handshake mismatch from " + ctx.channel().remoteAddress()
                                + ": expected id=" + protocol.getIdentifier() + " v=" + protocol.getVersion()
                                + " fp=" + protocol.getFingerprint().hex()
                                + ", got id=" + handshake.identifier() + " v=" + handshake.version()
                                + " fp=" + handshake.fingerprint().hex());
                        ctx.close();
                        return;
                    }

                    handshakeDone = true;

                    HandshakeMessage response = HandshakeCodec.fromProtocol(protocol);
                    ByteBuffer handshakePayload = ByteBuffer.allocate(256);
                    HandshakeCodec.encode(handshakePayload, response);
                    handshakePayload.flip();

                    int payloadLen = handshakePayload.remaining();
                    int lenSize = VarInt.varIntSize(payloadLen);
                    ByteBuffer frame = ByteBuffer.allocate(lenSize + payloadLen);
                    VarInt.writeVarInt(frame, payloadLen);
                    frame.put(handshakePayload);
                    frame.flip();

                    ctx.writeAndFlush(Unpooled.wrappedBuffer(frame));

                    if (nioBuf.hasRemaining()) {
                        io.netty.buffer.ByteBuf remaining = Unpooled.wrappedBuffer(bytes, nioBuf.position(), nioBuf.remaining());
                        ctx.fireChannelRead(remaining);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Handshake decode error from " + ctx.channel().remoteAddress(), e);
                    ctx.close();
                } finally {
                    buf.release();
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }

    static final class ServerChannelHandler extends ChannelInboundHandlerAdapter {
        private final Protocol protocol;
        private final MessageListener messageListener;

        ServerChannelHandler(Protocol protocol, MessageListener messageListener) {
            this.protocol = protocol;
            this.messageListener = messageListener;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            if (messageListener != null) {
                messageListener.onConnectionActive(ctx.channel());
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (messageListener != null) {
                messageListener.onConnectionInactive(ctx.channel());
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof Message) {
                if (messageListener != null) {
                    messageListener.onMessage((Message) msg, ctx.channel());
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.log(Level.WARNING, "Exception in channel " + ctx.channel().remoteAddress(), cause);
            ctx.close();
        }
    }
}