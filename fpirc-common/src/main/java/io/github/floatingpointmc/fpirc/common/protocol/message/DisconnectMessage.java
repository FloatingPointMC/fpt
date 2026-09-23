package io.github.floatingpointmc.fpirc.common.protocol.message;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class DisconnectMessage implements Message {
    private final String reason;
}