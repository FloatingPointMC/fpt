package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.Connection;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

@RequiredArgsConstructor
public final class ClientConnection implements Connection {
    private final @NotNull Channel channel;
    private final @NotNull SocketAddress client, local;

    public boolean isActive() {
        return channel.isActive();
    }

    public void send(@NotNull Message message) throws IllegalStateException {
        if (!isActive()) {
            throw new IllegalStateException("ClientConnection is closed");
        }
        channel.writeAndFlush(message);
    }

    public void close() {
        channel.close();
    }

    public @NotNull SocketAddress getRemoteAddress() {
        return client;
    }

    public @NotNull SocketAddress getLocalAddress(){
        return local;
    }
}
