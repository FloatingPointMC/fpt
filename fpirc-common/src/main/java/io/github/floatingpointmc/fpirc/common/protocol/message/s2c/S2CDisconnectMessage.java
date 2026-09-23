package io.github.floatingpointmc.fpirc.common.protocol.message.s2c;

import io.github.floatingpointmc.fpirc.common.protocol.S2CMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class S2CDisconnectMessage implements S2CMessage {
    private final String reason;
}