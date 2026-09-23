package io.github.floatingpointmc.fpirc.server.connection;

import io.github.floatingpointmc.fpirc.common.protocol.Message;

public interface MessageSender {

    void send(Message message);

    void close();
}