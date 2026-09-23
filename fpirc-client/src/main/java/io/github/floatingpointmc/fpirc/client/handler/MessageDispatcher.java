package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.common.protocol.message.ChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.DisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginResponseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MessageDispatcher {

    private static final Logger LOGGER = Logger.getLogger(MessageDispatcher.class.getName());

    private final Map<Class<?>, MessageHandler<?>> handlers = new HashMap<>();

    public MessageDispatcher() {
        registerDefaultHandlers();
    }

    public <T extends Message> void registerHandler(Class<T> messageType, MessageHandler<T> handler) {
        handlers.put(messageType, handler);
    }

    @SuppressWarnings("unchecked")
    public void dispatch(Message message) {
        MessageHandler<Message> handler = (MessageHandler<Message>) handlers.get(message.getClass());
        if (handler != null) {
            try {
                handler.handle(message);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error handling message: " + message.getClass().getSimpleName(), e);
            }
        } else {
            LOGGER.info("Received message: " + message);
        }
    }

    private void registerDefaultHandlers() {
        registerHandler(LoginResponseMessage.class, new LoginResponseMessageHandler());
        registerHandler(ChatMessage.class, new ChatMessageHandler());
        registerHandler(DisconnectMessage.class, new DisconnectMessageHandler());
    }
}