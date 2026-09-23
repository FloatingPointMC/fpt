package io.github.floatingpointmc.fpirc.common.protocol.message;

import io.github.floatingpointmc.fpirc.common.protocol.Message;

public final class LoginMessage implements Message {

    private final String username;

    public LoginMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}