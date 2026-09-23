package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CDisconnectMessage;

import java.util.logging.Logger;

public final class DisconnectMessageHandler implements MessageHandler<S2CDisconnectMessage> {

    private static final Logger LOGGER = Logger.getLogger(DisconnectMessageHandler.class.getName());

    @Override
    public void handle(S2CDisconnectMessage message) {
        LOGGER.info("Disconnected: " + message.getReason());
    }
}