package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.transport.Connection;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class FPTServer {
    private static final Logger LOGGER = Logger.getLogger(FPTServer.class.getName());
    final @NotNull CopyOnWriteArraySet<Connection> connections = new CopyOnWriteArraySet<>();
    @Unmodifiable
    private final @NotNull Channel serverChannel;
    private final @NotNull EventGroup eventGroup;
    @Unmodifiable
    private final boolean ownedEventGroup;

    void stop() {
        serverChannel.close().awaitUninterruptibly();
        if (ownedEventGroup) {
            eventGroup.close();
        }
        LOGGER.info("FPT Server stopped");
    }

    public boolean isRunning() {
        return serverChannel.isActive();
    }

    public int getPort() {
        return ((InetSocketAddress) serverChannel.localAddress()).getPort();
    }

    @Unmodifiable
    public @NotNull Set<Connection> getConnections() {
        return Collections.unmodifiableSet(connections);
    }
}