package io.github.floatingpointmc.fpt.client;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FPTClientFactory {
    public static final Protocol DEFAULT_PROTOCOL = Protocol.create();
    private final @NotNull String host;
    private final int port;
    private final @NotNull Protocol protocol;
    private final @NotNull EventGroup eventGroup;
    private final boolean ownedEventGroup;
    private final @NotNull List<ClientHandler> handlers;
    private @Nullable FPTClient runtime;

    public static @NotNull FPTClientFactory create(@NotNull String host, int port) {
        return create(host, port, DEFAULT_PROTOCOL);
    }

    public static @NotNull FPTClientFactory create(@NotNull String host, int port, @NotNull Protocol protocol) {
        return new FPTClientFactory(host, port, protocol, EventGroup.nio(), true, new ArrayList<>());
    }

    public static @NotNull FPTClientFactory create(@NotNull String host, int port, @NotNull EventGroup eventGroup) {
        return new FPTClientFactory(host, port, Protocol.create(), eventGroup, false, new ArrayList<>());
    }

    public static @NotNull FPTClientFactory create(@NotNull String host, int port,
                                                   @NotNull Protocol protocol,
                                                   @NotNull EventGroup eventGroup) {
        return new FPTClientFactory(host, port, protocol, eventGroup, false, new ArrayList<>());
    }

    public @NotNull FPTClientFactory handler(ClientHandler @NotNull ... handler) {
        if (runtime != null) {
            throw new IllegalStateException("Cannot set messenger after client has connected");
        }
        Collections.addAll(this.handlers, handler);
        return this;
    }

    public @NotNull FPTClient connect() {
        if (runtime != null) {
            throw new IllegalStateException("Client is already connected");
        }
        NettyClientTransport transport = new NettyClientTransport(protocol, eventGroup, ownedEventGroup, handlers);
        try {
            runtime = transport.connect(host, port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to connect", e);
        }
        return runtime;
    }
}