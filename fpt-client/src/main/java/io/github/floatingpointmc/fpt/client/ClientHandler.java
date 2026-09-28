package io.github.floatingpointmc.fpt.client;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.Connection;
import org.jetbrains.annotations.NotNull;

public interface ClientHandler {
    void onConnectionActive(@NotNull FPTClient client, @NotNull Connection connection);

    void onConnectionInactive(@NotNull FPTClient client, @NotNull Connection connection);

    void onMessage(@NotNull FPTClient client, @NotNull Connection connection, @NotNull Message message);
}
