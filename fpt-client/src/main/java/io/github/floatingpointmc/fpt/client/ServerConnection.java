package io.github.floatingpointmc.fpt.client;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.Connection;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ServerConnection implements Connection {
    private final @NotNull Channel channel;
    private final @NotNull SocketAddress server, local;

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public void send(@NotNull Message message) throws IllegalStateException {
        if (!isActive()) {
            throw new IllegalStateException("Connection is not active");
        }
        channel.writeAndFlush(message);
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public @NotNull SocketAddress getRemoteAddress() {
        return server;
    }

    @Override
    public @NotNull SocketAddress getLocalAddress() {
        return local;
    }
}
