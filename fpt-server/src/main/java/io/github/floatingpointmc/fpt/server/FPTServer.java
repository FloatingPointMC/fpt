package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.MessageListener;
import io.github.floatingpointmc.fpt.transport.netty.NettyServerTransport;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public final class FPTServer {
    private final @NotNull NettyServerTransport transport;
    @Getter
    private final @NotNull String host;
    private final int port;

    private FPTServer(@NotNull String host, int port, @NotNull Protocol protocol, @NotNull EventGroup eventGroup, boolean ownedEventGroup, MessageListener listener) {
        this.host = host;
        this.port = port;
        this.transport = new NettyServerTransport(protocol, eventGroup, ownedEventGroup, listener);
    }

    public static @NotNull FPTServer run(@NotNull String host, int port) {
        return run(host, port, Protocol.create());
    }

    public static @NotNull FPTServer run(@NotNull String host, int port, @NotNull Protocol protocol) {
        EventGroup eventGroup = EventGroup.nio();
        FPTServer server = new FPTServer(host, port, protocol, eventGroup, true, null);
        server.start();
        return server;
    }

    public static @NotNull FPTServer run(@NotNull String host, int port, @NotNull Protocol protocol, @NotNull EventGroup eventGroup, @NotNull MessageListener listener) {
        FPTServer server = new FPTServer(host, port, protocol, eventGroup, false, listener);
        server.start();
        return server;
    }

    private void start() {
        try {
            System.out.println("Server started on " + transport.start(host, port));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to start server", e);
        }
    }

    public void stop() {
        transport.stop();
    }

    public int getPort() {
        int actual = transport.getActualPort();
        return actual >= 0 ? actual : port;
    }

    public boolean isRunning() {
        return transport.getActualPort() >= 0;
    }
}