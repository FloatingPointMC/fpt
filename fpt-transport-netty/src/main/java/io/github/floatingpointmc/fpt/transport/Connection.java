package io.github.floatingpointmc.fpt.transport;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public interface Connection {
    boolean isActive();

    void send(@NotNull Message message) throws IllegalStateException;

    void close();

    @NotNull SocketAddress getRemoteAddress();

    @NotNull SocketAddress getLocalAddress();
}
