package io.github.floatingpointmc.fpt.client;

import io.github.floatingpointmc.fpt.codec.VarInt;
import io.github.floatingpointmc.fpt.protocol.*;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.Messenger;
import io.github.floatingpointmc.fpt.transport.netty.NettyFrameDecoder;
import io.github.floatingpointmc.fpt.transport.netty.NettyMessageDecoder;
import io.github.floatingpointmc.fpt.transport.netty.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class NettyClientTransport {
    private static final Logger LOGGER = Logger.getLogger(NettyClientTransport.class.getName());

    private static final long HANDSHAKE_TIMEOUT_SECONDS = 10;

    private final @NotNull Protocol protocol;
    private final @NotNull EventGroup eventGroup;
    private final boolean ownedEventGroup;
    private final @NotNull Messenger messenger;

    private Channel channel;
    private volatile boolean handshakeComplete = false;

    public void connect(@NotNull String host, int port) throws InterruptedException {
        CountDownLatch handshakeLatch = new CountDownLatch(1);
        AtomicReference<Throwable> handshakeError = new AtomicReference<>();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventGroup.workerGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("frame-decoder", new NettyFrameDecoder(ProtocolConstants.MAX_PACKET_SIZE));
                        pipeline.addLast("handshake-handler", new ClientHandshakeHandler(protocol, handshakeLatch, handshakeError));
                        pipeline.addLast("s2c-decoder", new NettyMessageDecoder(protocol, MessageDirection.S2C));
                        pipeline.addLast("c2s-encoder", new NettyMessageEncoder(protocol));
                        pipeline.addLast("handler", new ClientChannelHandler(messenger));
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();

        if (!handshakeLatch.await(HANDSHAKE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            channel.close();
            if (ownedEventGroup) eventGroup.close();
            throw new RuntimeException("Handshake timeout");
        }

        Throwable error = handshakeError.get();
        if (error != null) {
            channel.close();
            if (ownedEventGroup) eventGroup.close();
            throw new RuntimeException("Handshake failed", error);
        }

        handshakeComplete = true;
        messenger.onConnectionActive(channel);
        LOGGER.info("Connected to " + host + ":" + port + " (handshake OK)");
    }

    public void disconnect() {
        if (channel != null) {
            channel.close().awaitUninterruptibly();
        }
        if (ownedEventGroup) {
            eventGroup.close();
        }
        handshakeComplete = false;
        LOGGER.info("Disconnected");
    }

    public boolean isConnected() {
        return channel != null && channel.isActive() && handshakeComplete;
    }

    static final class ClientHandshakeHandler extends ChannelInboundHandlerAdapter {
        private final Protocol protocol;
        private final CountDownLatch latch;
        private final AtomicReference<Throwable> error;

        ClientHandshakeHandler(Protocol protocol, CountDownLatch latch, AtomicReference<Throwable> error) {
            this.protocol = protocol;
            this.latch = latch;
            this.error = error;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            HandshakeMessage handshake = HandshakeCodec.fromProtocol(protocol);
            ByteBuffer handshakePayload = ByteBuffer.allocate(256);
            HandshakeCodec.encode(handshakePayload, handshake);
            handshakePayload.flip();

            int payloadLen = handshakePayload.remaining();
            int lenSize = VarInt.varIntSize(payloadLen);
            ByteBuffer frame = ByteBuffer.allocate(lenSize + payloadLen);
            VarInt.writeVarInt(frame, payloadLen);
            frame.put(handshakePayload);
            frame.flip();

            ctx.writeAndFlush(Unpooled.wrappedBuffer(frame));
            ctx.fireChannelActive();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                try {
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    ByteBuffer nioBuf = ByteBuffer.wrap(bytes);

                    HandshakeMessage serverHandshake = HandshakeCodec.decode(nioBuf);

                    if (!serverHandshake.matches(protocol)) {
                        error.compareAndSet(null, new RuntimeException(
                                "Handshake mismatch: expected id=" + protocol.getIdentifier()
                                        + " v=" + protocol.getVersion()
                                        + " fp=" + protocol.getFingerprint().hex()
                                        + ", got id=" + serverHandshake.identifier()
                                        + " v=" + serverHandshake.version()
                                        + " fp=" + serverHandshake.fingerprint().hex()));
                        latch.countDown();
                        ctx.close();
                        return;
                    }

                    latch.countDown();

                    if (nioBuf.hasRemaining()) {
                        ByteBuf remaining = Unpooled.wrappedBuffer(bytes, nioBuf.position(), nioBuf.remaining());
                        ctx.fireChannelRead(remaining);
                    }
                } catch (Exception e) {
                    error.compareAndSet(null, e);
                    latch.countDown();
                    ctx.close();
                } finally {
                    buf.release();
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            error.compareAndSet(null, new RuntimeException("Connection closed before handshake completed"));
            latch.countDown();
            ctx.fireChannelInactive();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            error.compareAndSet(null, cause);
            latch.countDown();
            ctx.close();
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class ClientChannelHandler extends ChannelInboundHandlerAdapter {
        private final @NotNull Messenger messenger;

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            LOGGER.info("Channel active: " + ctx.channel().remoteAddress());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            messenger.onConnectionInactive(ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof Message) {
                messenger.onMessage((Message) msg);
            } else {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.log(Level.WARNING, "Exception in client channel", cause);
            ctx.close();
        }
    }
}