package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.Connection;
import org.jetbrains.annotations.NotNull;

public interface ServerHandler {
    void onConnectionActive(@NotNull FPTServer server, @NotNull Connection connection);

    void onConnectionInactive(@NotNull FPTServer server, @NotNull Connection connection);

    void onMessage(@NotNull FPTServer server, @NotNull Connection connection, @NotNull Message message);
}
