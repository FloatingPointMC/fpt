package io.github.floatingpointmc.fpirc.server.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage;
import io.github.floatingpointmc.fpirc.server.connection.Connection;
import io.github.floatingpointmc.fpirc.server.connection.ConnectionManager;
import io.github.floatingpointmc.fpirc.server.session.Session;

import java.util.logging.Logger;

public final class ChatMessageHandler implements MessageHandler<C2SChatMessage> {

    private static final Logger LOGGER = Logger.getLogger(ChatMessageHandler.class.getName());

    private final ConnectionManager connectionManager;

    public ChatMessageHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void handle(C2SChatMessage message, Connection connection) {
        Session session = connection.getSession();
        if (session == null || !session.isAuthenticated()) {
            LOGGER.warning("Unauthenticated chat attempt from " + connection.getRemoteAddress());
            return;
        }

        String sender = session.getUsername();
        S2CChatMessage broadcast = new S2CChatMessage(message.getChannel(), sender + ": " + message.getContent());

        for (Connection conn : connectionManager.getAll()) {
            Session peerSession = conn.getSession();
            if (peerSession != null && peerSession.isAuthenticated()) {
                conn.send(broadcast);
            }
        }

        LOGGER.info("Chat in " + message.getChannel() + " from " + sender + ": " + message.getContent());
    }
}