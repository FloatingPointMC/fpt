package io.github.floatingpointmc.fpirc.client.transport.netty;

import io.github.floatingpointmc.fpirc.client.handler.MessageDispatcher;
import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public final class NettyClient {

    private static final Logger LOGGER = Logger.getLogger(NettyClient.class.getName());

    private static final long HANDSHAKE_TIMEOUT_SECONDS = 10;

    private final String url;
    private final MessageRegistry messageRegistry;
    private final MessageDispatcher messageDispatcher;

    private EventLoopGroup eventLoopGroup;
    private Channel channel;

    public NettyClient(String url, MessageRegistry messageRegistry, MessageDispatcher messageDispatcher) {
        this.url = url;
        this.messageRegistry = messageRegistry;
        this.messageDispatcher = messageDispatcher;
    }

    public void connect() throws InterruptedException {
        URI uri = URI.create(url);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = "wss".equals(scheme) ? 443 : 80;
        }

        eventLoopGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, null, 65536);

        CountDownLatch handshakeLatch = new CountDownLatch(1);
        AtomicReference<Throwable> handshakeError = new AtomicReference<>();

        WebSocketClientProtocolHandler wsHandler =
                new WebSocketClientProtocolHandler(handshaker);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
                        pipeline.addLast("ws-protocol", wsHandler);
                        pipeline.addLast("ws-handshake-watcher", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                                    handshakeLatch.countDown();
                                }
                                ctx.fireUserEventTriggered(evt);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                handshakeError.compareAndSet(null, cause);
                                handshakeLatch.countDown();
                                ctx.close();
                            }
                        });
                        pipeline.addLast("ws-frame-adapter", new WebSocketFrameAdapter());
                        pipeline.addLast("fpirc-frame-decoder",
                                new NettyFrameDecoder(ProtocolConstants.MAX_PACKET_SIZE));
                        pipeline.addLast("fpirc-message-decoder",
                                new NettyMessageDecoder(messageRegistry));
                        pipeline.addLast("ws-frame-wrapper", new WebSocketFrameWrapper());
                        pipeline.addLast("fpirc-message-encoder",
                                new NettyMessageEncoder(messageRegistry));
                        pipeline.addLast("fpirc-handler",
                                new NettyClientHandler(messageDispatcher));
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();

        if (!handshakeLatch.await(HANDSHAKE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            channel.close();
            eventLoopGroup.shutdownGracefully();
            throw new RuntimeException("WebSocket handshake timed out");
        }

        Throwable error = handshakeError.get();
        if (error != null) {
            channel.close();
            eventLoopGroup.shutdownGracefully();
            throw new RuntimeException("WebSocket handshake failed", error);
        }

        LOGGER.info("FPIRC Client connected to " + url);
    }

    public void send(Message message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    public void disconnect() {
        if (channel != null) {
            channel.close().awaitUninterruptibly();
        }
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        LOGGER.info("FPIRC Client disconnected");
    }
}