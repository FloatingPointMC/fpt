package io.github.floatingpointmc.fpt.client;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.Messenger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public final class FPTClient {
    public static final Protocol DEFAULT_PROTOCOL = Protocol.create();

    @Getter
    private final @NotNull String host;
    @Getter
    private final int port;
    private final @NotNull Protocol protocol;
    private final @NotNull EventGroup eventGroup;
    private final boolean ownedEventGroup;
    private @NotNull Messenger messenger;

    private @NotNull NettyClientTransport transport;
    private volatile boolean connected = false;

    private FPTClient(@NotNull String host, int port, @NotNull Protocol protocol,
                      @NotNull EventGroup eventGroup, boolean ownedEventGroup,
                      @NotNull Messenger messenger) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.eventGroup = eventGroup;
        this.ownedEventGroup = ownedEventGroup;
        this.messenger = messenger;
        this.transport = new NettyClientTransport(protocol, eventGroup, ownedEventGroup, messenger);
    }

    public static @NotNull FPTClient create(@NotNull String host, int port) {
        return create(host, port, DEFAULT_PROTOCOL);
    }

    public static @NotNull FPTClient create(@NotNull String host, int port, @NotNull Protocol protocol) {
        return new FPTClient(host, port, protocol, EventGroup.nio(), true, Messenger.empty());
    }

    public static @NotNull FPTClient create(@NotNull String host, int port, @NotNull EventGroup eventGroup) {
        return new FPTClient(host, port, Protocol.create(), eventGroup, false, Messenger.empty());
    }

    public static @NotNull FPTClient create(@NotNull String host, int port,
                                            @NotNull Protocol protocol,
                                            @NotNull EventGroup eventGroup) {
        return new FPTClient(host, port, protocol, eventGroup, false, Messenger.empty());
    }

    public void messenger(@NotNull Messenger messenger) {
        if (connected) {
            throw new IllegalStateException("Cannot set messenger after client has connected");
        }
        this.messenger = messenger;
        this.transport = new NettyClientTransport(protocol, eventGroup, ownedEventGroup, messenger);
    }

    public void connect() {
        if (connected) {
            throw new IllegalStateException("Client is already connected");
        }
        try {
            transport.connect(host, port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to connect", e);
        }
        connected = true;
    }

    public void disconnect() {
        if (!connected) {
            return;
        }
        connected = false;
        transport.disconnect();
    }

    public boolean isConnected() {
        return transport.isConnected();
    }
}