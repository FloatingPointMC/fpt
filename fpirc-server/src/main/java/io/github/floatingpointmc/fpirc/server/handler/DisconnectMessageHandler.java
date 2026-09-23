package io.github.floatingpointmc.fpirc.server.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.DisconnectMessage;
import io.github.floatingpointmc.fpirc.server.connection.Connection;
import io.github.floatingpointmc.fpirc.server.session.Session;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class DisconnectMessageHandler implements MessageHandler<DisconnectMessage> {

    private static final Logger LOGGER = Logger.getLogger(DisconnectMessageHandler.class.getName());

    @Override
    public void handle(DisconnectMessage message, Connection connection) {
        Session session = connection.getSession();
        String username = session != null ? session.getUsername() : "unknown";

        LOGGER.info("User disconnecting: " + username + " reason: " + message.getReason());

        if (session != null) {
            session.close();
        }
        connection.disconnect();
    }
}