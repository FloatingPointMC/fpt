package io.github.floatingpointmc.fpirc.server.handler;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.server.connection.Connection;

public interface MessageHandler<T extends Message> {

    void handle(T message, Connection connection);
}