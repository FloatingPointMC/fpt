package io.github.floatingpointmc.fpt.client;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.Messenger;
import io.github.floatingpointmc.fpt.transport.netty.NettyClientTransport;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public final class FPTClient {
    public static final Protocol DEFAULT_PROTOCOL = Protocol.create();

    private final @NotNull NettyClientTransport transport;
    @Getter
    private final @NotNull String host;
    @Getter
    private final int port;

    private FPTClient(@NotNull String host, int port, @NotNull Protocol protocol, @NotNull EventGroup eventGroup, boolean ownedEventGroup, @NotNull Messenger listener) {
        this.host = host;
        this.port = port;
        this.transport = new NettyClientTransport(protocol, eventGroup, ownedEventGroup, listener);
    }

    public static @NotNull FPTClient connect(@NotNull String host, int port) {
        return connect(host, port, DEFAULT_PROTOCOL);
    }

    public static @NotNull FPTClient connect(@NotNull String host, int port, @NotNull Protocol protocol) {
        EventGroup eventGroup = EventGroup.nio();
        FPTClient client = new FPTClient(host, port, protocol, eventGroup, true, Messenger.empty());
        client.doConnect();
        return client;
    }

    public static @NotNull FPTClient connect(@NotNull String host, int port, @NotNull Protocol protocol, @NotNull EventGroup eventGroup, @NotNull Messenger listener) {
        FPTClient client = new FPTClient(host, port, protocol, eventGroup, false, listener);
        client.doConnect();
        return client;
    }

    private void doConnect() {
        try {
            transport.connect(host, port);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to connect", e);
        }
    }

    public void disconnect() {
        transport.disconnect();
    }

    public boolean isConnected() {
        return transport.isConnected();
    }
}