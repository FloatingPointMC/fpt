package io.github.floatingpointmc.fpirc.server.handler;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;
import io.github.floatingpointmc.fpirc.common.protocol.message.ChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.DisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginMessage;
import io.github.floatingpointmc.fpirc.server.connection.Connection;
import io.github.floatingpointmc.fpirc.server.connection.ConnectionManager;
import io.github.floatingpointmc.fpirc.server.session.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MessageDispatcher {

    private static final Logger LOGGER = Logger.getLogger(MessageDispatcher.class.getName());

    private final Map<Class<?>, MessageHandler<?>> handlers = new HashMap<>();
    private final ConnectionManager connectionManager;

    public MessageDispatcher(MessageRegistry registry, ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        registerDefaultHandlers();
    }

    public <T extends Message> void registerHandler(Class<T> messageType, MessageHandler<T> handler) {
        handlers.put(messageType, handler);
    }

    @SuppressWarnings("unchecked")
    public void dispatch(Message message, Connection connection) {
        MessageHandler<Message> handler = (MessageHandler<Message>) handlers.get(message.getClass());
        if (handler != null) {
            try {
                handler.handle(message, connection);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error handling message " + message.getClass().getSimpleName()
                        + " from " + connection, e);
            }
        } else {
            LOGGER.warning("No handler registered for message type: " + message.getClass().getSimpleName());
        }
    }

    public void onConnectionActive(Connection connection) {
        connectionManager.add(connection);
        LOGGER.info("Connection established: " + connection);
    }

    public void onConnectionInactive(Connection connection) {
        connectionManager.remove(connection.getId());
        Session session = connection.getSession();
        if (session != null) {
            session.close();
        }
        LOGGER.info("Connection closed: " + connection);
    }

    private void registerDefaultHandlers() {
        registerHandler(LoginMessage.class, new LoginMessageHandler());
        registerHandler(ChatMessage.class, new ChatMessageHandler(connectionManager));
        registerHandler(DisconnectMessage.class, new DisconnectMessageHandler());
    }
}