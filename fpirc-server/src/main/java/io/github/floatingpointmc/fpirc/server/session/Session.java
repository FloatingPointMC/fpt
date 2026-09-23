package io.github.floatingpointmc.fpirc.server.session;

import java.util.concurrent.atomic.AtomicReference;

public final class Session {

    private final AtomicReference<SessionState> state;
    private volatile String username;

    public Session() {
        this.state = new AtomicReference<>(SessionState.CONNECTED);
    }

    public SessionState getState() {
        return state.get();
    }

    public boolean transitionTo(SessionState newState) {
        SessionState current = state.get();
        if (isValidTransition(current, newState)) {
            return state.compareAndSet(current, newState);
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAuthenticated() {
        return state.get() == SessionState.AUTHENTICATED;
    }

    public void close() {
        state.set(SessionState.CLOSED);
    }

    private boolean isValidTransition(SessionState from, SessionState to) {
        return switch (from) {
            case CONNECTED -> to == SessionState.AUTHENTICATING || to == SessionState.CLOSED;
            case AUTHENTICATING -> to == SessionState.AUTHENTICATED || to == SessionState.CLOSED;
            case AUTHENTICATED -> to == SessionState.CLOSED;
            case CLOSED -> false;
        };
    }
}