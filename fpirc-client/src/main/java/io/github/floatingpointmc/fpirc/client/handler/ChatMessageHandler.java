package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.ChatMessage;

import java.util.logging.Logger;

public final class ChatMessageHandler implements MessageHandler<ChatMessage> {

    private static final Logger LOGGER = Logger.getLogger(ChatMessageHandler.class.getName());

    @Override
    public void handle(ChatMessage message) {
        LOGGER.info("[" + message.getChannel() + "] " + message.getContent());
    }
}