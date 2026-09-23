package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.S2CMessage;

public interface MessageHandler<T extends S2CMessage> {

    void handle(T message);
}