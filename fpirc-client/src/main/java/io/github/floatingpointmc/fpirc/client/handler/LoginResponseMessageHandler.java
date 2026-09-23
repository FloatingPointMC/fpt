package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CLoginResponseMessage;

import java.util.logging.Logger;

public final class LoginResponseMessageHandler implements MessageHandler<S2CLoginResponseMessage> {

    private static final Logger LOGGER = Logger.getLogger(LoginResponseMessageHandler.class.getName());

    @Override
    public void handle(S2CLoginResponseMessage message) {
        if (message.isSuccess()) {
            LOGGER.info("Login successful: " + message.getReason());
        } else {
            LOGGER.warning("Login failed: " + message.getReason());
        }
    }
}