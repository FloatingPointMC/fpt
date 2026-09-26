package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.Messenger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FPTServer {
    @Getter
    private final @NotNull String host;
    private final int port;
    private final @NotNull Protocol protocol;
    private final @NotNull EventGroup eventGroup;
    private final boolean ownedEventGroup;
    private final @NotNull List<Messenger> messengers;

    private @Nullable NettyServerRuntime runtime;
    @Getter
    private volatile boolean running = false;

    private FPTServer(@NotNull String host, int port, @NotNull Protocol protocol,
                      @NotNull EventGroup eventGroup, boolean ownedEventGroup,
                      @NotNull List<Messenger> messengers) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.eventGroup = eventGroup;
        this.ownedEventGroup = ownedEventGroup;
        this.messengers = new ArrayList<>(messengers);
    }

    public static @NotNull FPTServer create(@NotNull String host, int port) {
        return create(host, port, Protocol.create());
    }

    public static @NotNull FPTServer create(@NotNull String host, int port, @NotNull Protocol protocol) {
        return new FPTServer(host, port, protocol, EventGroup.nio(), true, Collections.emptyList());
    }

    public static @NotNull FPTServer create(@NotNull String host, int port, @NotNull EventGroup eventGroup) {
        return new FPTServer(host, port, Protocol.create(), eventGroup, false, Collections.emptyList());
    }

    public static @NotNull FPTServer create(@NotNull String host, int port,
                                            @NotNull Protocol protocol,
                                            @NotNull EventGroup eventGroup) {
        return new FPTServer(host, port, protocol, eventGroup, false, Collections.emptyList());
    }

    public void messenger(Messenger @NotNull ... messenger) {
        if (running) {
            throw new IllegalStateException("Cannot set messenger after server has started");
        }
        this.messengers.clear();
        Collections.addAll(this.messengers, messenger);
    }

    public void run() {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }
        NettyServerTransport transport = new NettyServerTransport(protocol, eventGroup, ownedEventGroup, messengers);
        try {
            this.runtime = transport.start(host, port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to start server", e);
        }
        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (runtime != null) {
            runtime.stop();
            runtime = null;
        }
    }

    public int getPort() {
        if (runtime != null) {
            return runtime.getActualPort();
        }
        return port;
    }
}