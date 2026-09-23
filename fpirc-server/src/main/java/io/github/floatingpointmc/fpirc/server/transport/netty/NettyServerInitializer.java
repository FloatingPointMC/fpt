package io.github.floatingpointmc.fpirc.server.transport.netty;

import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.server.connection.ConnectionManager;
import io.github.floatingpointmc.fpirc.server.handler.MessageDispatcher;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public final class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/";

    private final MessageRegistry messageRegistry;
    private final ConnectionManager connectionManager;
    private final MessageDispatcher messageDispatcher;

    public NettyServerInitializer(MessageRegistry messageRegistry,
                                  ConnectionManager connectionManager,
                                  MessageDispatcher messageDispatcher) {
        this.messageRegistry = messageRegistry;
        this.connectionManager = connectionManager;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("http-codec", new HttpServerCodec());
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("idle-state", new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast("ws-protocol", new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        pipeline.addLast("ws-frame-adapter", new WebSocketFrameAdapter());
        pipeline.addLast("fpirc-frame-decoder", new NettyFrameDecoder(ProtocolConstants.MAX_PACKET_SIZE));
        pipeline.addLast("fpirc-message-decoder", new NettyMessageDecoder(messageRegistry));
        pipeline.addLast("ws-frame-wrapper", new WebSocketFrameWrapper());
        pipeline.addLast("fpirc-message-encoder", new NettyMessageEncoder(messageRegistry));
        pipeline.addLast("fpirc-handler", new NettyServerHandler(messageDispatcher));
    }
}