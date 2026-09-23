package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.DisconnectMessage;

import java.util.logging.Logger;

public final class DisconnectMessageHandler implements MessageHandler<DisconnectMessage> {

    private static final Logger LOGGER = Logger.getLogger(DisconnectMessageHandler.class.getName());

    @Override
    public void handle(DisconnectMessage message) {
        LOGGER.info("Disconnected: " + message.getReason());
    }
}