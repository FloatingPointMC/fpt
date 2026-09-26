package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.netty.channel.Channel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

final class NettyServerRuntime {
    private static final Logger LOGGER = Logger.getLogger(NettyServerRuntime.class.getName());

    @Getter
    private final @NotNull Channel serverChannel;
    @Getter
    private final int actualPort;
    @Getter
    private final @NotNull CopyOnWriteArraySet<Channel> channels = new CopyOnWriteArraySet<>();
    private final @NotNull EventGroup eventGroup;
    private final boolean ownedEventGroup;

    NettyServerRuntime(@NotNull Channel serverChannel, @NotNull EventGroup eventGroup, boolean ownedEventGroup) {
        this.serverChannel = serverChannel;
        this.actualPort = ((InetSocketAddress) serverChannel.localAddress()).getPort();
        this.eventGroup = eventGroup;
        this.ownedEventGroup = ownedEventGroup;
    }

    void stop() {
        serverChannel.close().awaitUninterruptibly();
        if (ownedEventGroup) {
            eventGroup.close();
        }
        LOGGER.info("FPT Server stopped");
    }
}