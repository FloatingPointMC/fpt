package io.github.floatingpointmc.fpirc.common.protocol;

import io.github.floatingpointmc.fpirc.common.protocol.codec.c2s.C2SChatMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.c2s.C2SDisconnectMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.c2s.C2SLoginMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.s2c.S2CChatMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.s2c.S2CDisconnectMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.s2c.S2CLoginResponseMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SDisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SLoginMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SMessageIds;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CDisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CLoginResponseMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CMessageIds;

public final class DefaultMessageRegistry {

    private DefaultMessageRegistry() {
    }

    public static MessageRegistry createC2S() {
        MessageRegistry registry = new MessageRegistry();
        registry.register(C2SMessageIds.LOGIN, C2SLoginMessage.class, new C2SLoginMessageCodec());
        registry.register(C2SMessageIds.CHAT, C2SChatMessage.class, new C2SChatMessageCodec());
        registry.register(C2SMessageIds.DISCONNECT, C2SDisconnectMessage.class, new C2SDisconnectMessageCodec());
        return registry;
    }

    public static MessageRegistry createS2C() {
        MessageRegistry registry = new MessageRegistry();
        registry.register(S2CMessageIds.LOGIN_RESPONSE, S2CLoginResponseMessage.class, new S2CLoginResponseMessageCodec());
        registry.register(S2CMessageIds.CHAT, S2CChatMessage.class, new S2CChatMessageCodec());
        registry.register(S2CMessageIds.DISCONNECT, S2CDisconnectMessage.class, new S2CDisconnectMessageCodec());
        return registry;
    }
}