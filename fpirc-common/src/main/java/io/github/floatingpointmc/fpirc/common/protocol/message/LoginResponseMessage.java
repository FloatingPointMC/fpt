package io.github.floatingpointmc.fpirc.common.protocol.message;

import io.github.floatingpointmc.fpirc.common.protocol.Message;

public final class LoginResponseMessage implements Message {

    private final boolean success;
    private final String reason;

    public LoginResponseMessage(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }
}