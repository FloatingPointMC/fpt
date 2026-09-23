package io.github.floatingpointmc.fpirc.server.handler;

import io.github.floatingpointmc.fpirc.common.protocol.C2SMessage;
import io.github.floatingpointmc.fpirc.server.connection.Connection;

public interface MessageHandler<T extends C2SMessage> {

    void handle(T message, Connection connection);
}