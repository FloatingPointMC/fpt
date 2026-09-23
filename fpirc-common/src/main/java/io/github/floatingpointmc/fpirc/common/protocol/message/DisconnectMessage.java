package io.github.floatingpointmc.fpirc.common.protocol.message;

import io.github.floatingpointmc.fpirc.common.protocol.Message;

public final class DisconnectMessage implements Message {

    private final String reason;

    public DisconnectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}