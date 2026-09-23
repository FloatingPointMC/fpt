package io.github.floatingpointmc.fpirc.server.session;

public enum SessionState {
    CONNECTED,
    AUTHENTICATING,
    AUTHENTICATED,
    CLOSED
}