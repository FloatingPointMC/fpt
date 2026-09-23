package io.github.floatingpointmc.fpirc.server.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SLoginMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CLoginResponseMessage;
import io.github.floatingpointmc.fpirc.server.connection.Connection;
import io.github.floatingpointmc.fpirc.server.session.Session;
import io.github.floatingpointmc.fpirc.server.session.SessionState;

import java.util.logging.Logger;

public final class LoginMessageHandler implements MessageHandler<C2SLoginMessage> {

    private static final Logger LOGGER = Logger.getLogger(LoginMessageHandler.class.getName());

    @Override
    public void handle(C2SLoginMessage message, Connection connection) {
        Session session = connection.getSession();
        if (session == null) {
            session = new Session();
            connection.setSession(session);
        }

        if (session.isAuthenticated()) {
            connection.send(new S2CLoginResponseMessage(false, "Already authenticated"));
            return;
        }

        if (!session.transitionTo(SessionState.AUTHENTICATING)) {
            connection.send(new S2CLoginResponseMessage(false, "Invalid session state"));
            return;
        }

        String username = message.getUsername();

        session.setUsername(username);
        session.transitionTo(SessionState.AUTHENTICATED);

        connection.send(new S2CLoginResponseMessage(true, "Welcome, " + username));

        LOGGER.info("User logged in: " + username + " from " + connection.getRemoteAddress());
    }
}