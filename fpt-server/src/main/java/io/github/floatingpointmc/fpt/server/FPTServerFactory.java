package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FPTServerFactory {
    private final @NotNull String host;
    private final int port;
    private final @NotNull Protocol protocol;
    private final @NotNull EventGroup eventGroup;
    private final boolean ownedEventGroup;
    private final @NotNull List<ServerHandler> handlers;
    private @Nullable FPTServer runtime;

    private FPTServerFactory(@NotNull String host, int port, @NotNull Protocol protocol,
                             @NotNull EventGroup eventGroup, boolean ownedEventGroup,
                             @NotNull List<ServerHandler> handlers) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.eventGroup = eventGroup;
        this.ownedEventGroup = ownedEventGroup;
        this.handlers = new ArrayList<>(handlers);
    }

    public static @NotNull FPTServerFactory create(@NotNull String host, int port) {
        return create(host, port, Protocol.create());
    }

    public static @NotNull FPTServerFactory create(@NotNull String host, int port, @NotNull Protocol protocol) {
        return new FPTServerFactory(host, port, protocol, EventGroup.nio(), true, Collections.emptyList());
    }

    public static @NotNull FPTServerFactory create(@NotNull String host, int port, @NotNull EventGroup eventGroup) {
        return new FPTServerFactory(host, port, Protocol.create(), eventGroup, false, Collections.emptyList());
    }

    public static @NotNull FPTServerFactory create(@NotNull String host, int port,
                                                   @NotNull Protocol protocol,
                                                   @NotNull EventGroup eventGroup) {
        return new FPTServerFactory(host, port, protocol, eventGroup, false, Collections.emptyList());
    }

    public @NotNull FPTServerFactory handler(ServerHandler @NotNull ... handler) {
        if (runtime != null) {
            throw new IllegalStateException("Cannot set messenger after server has started");
        }
        Collections.addAll(this.handlers, handler);
        return this;
    }

    public @NotNull FPTServer run() {
        if (runtime != null) {
            throw new IllegalStateException("Server is already running");
        }
        NettyServerTransport transport = new NettyServerTransport(protocol, eventGroup, ownedEventGroup, handlers);
        try {
            this.runtime = transport.start(host, port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to start server", e);
        }
        return runtime;
    }
}