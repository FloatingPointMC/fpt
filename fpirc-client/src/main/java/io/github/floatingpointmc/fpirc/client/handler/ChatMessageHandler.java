package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage;

import java.util.logging.Logger;

public final class ChatMessageHandler implements MessageHandler<S2CChatMessage> {

    private static final Logger LOGGER = Logger.getLogger(ChatMessageHandler.class.getName());

    @Override
    public void handle(S2CChatMessage message) {
        LOGGER.info("[" + message.getChannel() + "] " + message.getContent());
    }
}