package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.Messenger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FPTServer {
    @Getter
    private final @NotNull String host;
    private final int port;
    private final @NotNull NettyServerTransport transport;
    @Getter
    private volatile boolean running = false;

    private FPTServer(@NotNull String host, int port, @NotNull Protocol protocol,
                      @NotNull EventGroup eventGroup, boolean ownedEventGroup,
                      @NotNull List<Messenger> messenger) {
        this.host = host;
        this.port = port;
        this.transport = new NettyServerTransport(protocol, eventGroup, ownedEventGroup, messenger);
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

    public @NotNull FPTServer messenger(@NotNull Messenger... messenger) {
        if (running) {
            throw new IllegalStateException("Cannot set messenger after server has started");
        }
        return new FPTServer(host, port, transport.getProtocol(), transport.getEventGroup(),
                transport.isOwnedEventGroup(), Arrays.asList(messenger));
    }

    public void run() {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }
        running = true;
        try {
            transport.start(host, port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
            throw new RuntimeException("Failed to start server", e);
        }
    }

    public void stop() {
        running = false;
        transport.stop();
    }

    public int getPort() {
        int actual = transport.getActualPort();
        return actual >= 0 ? actual : port;
    }
}