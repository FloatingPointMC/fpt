package io.github.floatingpointmc.fpirc.client.handler;

import io.github.floatingpointmc.fpirc.common.protocol.S2CMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CDisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CLoginResponseMessage;

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

    public <T extends S2CMessage> void registerHandler(Class<T> messageType, MessageHandler<T> handler) {
        handlers.put(messageType, handler);
    }

    @SuppressWarnings("unchecked")
    public void dispatch(S2CMessage message) {
        MessageHandler<S2CMessage> handler = (MessageHandler<S2CMessage>) handlers.get(message.getClass());
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
        registerHandler(S2CLoginResponseMessage.class, new LoginResponseMessageHandler());
        registerHandler(S2CChatMessage.class, new ChatMessageHandler());
        registerHandler(S2CDisconnectMessage.class, new DisconnectMessageHandler());
    }
}