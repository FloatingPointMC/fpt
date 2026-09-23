package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.Message;

public interface MessageHandler<T extends Message> {

    void handle(T message);
}