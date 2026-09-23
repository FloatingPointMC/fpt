package io.github.floatingpointmc.fpirc.common.protocol.message.c2s;

import io.github.floatingpointmc.fpirc.common.protocol.C2SMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class C2SLoginMessage implements C2SMessage {
    private final String username;
}