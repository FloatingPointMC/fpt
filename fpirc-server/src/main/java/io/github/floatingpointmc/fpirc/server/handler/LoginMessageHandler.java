package io.github.floatingpointmc.fpirc.server.handler;

import io.github.floatingpointmc.fpirc.common.protocol.message.LoginMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginResponseMessage;
import io.github.floatingpointmc.fpirc.server.connection.Connection;
import io.github.floatingpointmc.fpirc.server.session.Session;
import io.github.floatingpointmc.fpirc.server.session.SessionState;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoginMessageHandler implements MessageHandler<LoginMessage> {

    private static final Logger LOGGER = Logger.getLogger(LoginMessageHandler.class.getName());

    @Override
    public void handle(LoginMessage message, Connection connection) {
        Session session = connection.getSession();
        if (session == null) {
            session = new Session();
            connection.setSession(session);
        }

        if (session.isAuthenticated()) {
            connection.send(new LoginResponseMessage(false, "Already authenticated"));
            return;
        }

        if (!session.transitionTo(SessionState.AUTHENTICATING)) {
            connection.send(new LoginResponseMessage(false, "Invalid session state"));
            return;
        }

        String username = message.getUsername();

        session.setUsername(username);
        session.transitionTo(SessionState.AUTHENTICATED);

        connection.send(new LoginResponseMessage(true, "Welcome, " + username));

        LOGGER.info("User logged in: " + username + " from " + connection.getRemoteAddress());
    }
}