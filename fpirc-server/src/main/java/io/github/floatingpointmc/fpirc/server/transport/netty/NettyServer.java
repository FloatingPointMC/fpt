package io.github.floatingpointmc.fpirc.server.transport.netty;

import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;
import io.github.floatingpointmc.fpirc.server.connection.ConnectionManager;
import io.github.floatingpointmc.fpirc.server.handler.MessageDispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.logging.Logger;

public final class NettyServer {

    private static final Logger LOGGER = Logger.getLogger(NettyServer.class.getName());

    private final String host;
    private final int port;
    private final MessageRegistry c2sRegistry;
    private final MessageRegistry s2cRegistry;
    private final ConnectionManager connectionManager;
    private final MessageDispatcher messageDispatcher;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyServer(String host, int port, MessageRegistry c2sRegistry, MessageRegistry s2cRegistry,
                       ConnectionManager connectionManager, MessageDispatcher messageDispatcher) {
        this.host = host;
        this.port = port;
        this.c2sRegistry = c2sRegistry;
        this.s2cRegistry = s2cRegistry;
        this.connectionManager = connectionManager;
        this.messageDispatcher = messageDispatcher;
    }

    public void start() throws InterruptedException {
        bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyServerInitializer(c2sRegistry, s2cRegistry, connectionManager, messageDispatcher))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(host, port).sync();
        serverChannel = future.channel();

        LOGGER.info("FPIRC Server started on " + host + ":" + getActualPort());
    }

    public void stop() {
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        LOGGER.info("FPIRC Server stopped");
    }

    public int getActualPort() {
        if (serverChannel != null) {
            return ((java.net.InetSocketAddress) serverChannel.localAddress()).getPort();
        }
        return port;
    }
}