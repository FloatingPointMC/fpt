package io.github.floatingpointmc.fpirc.common.protocol;

import io.github.floatingpointmc.fpirc.common.protocol.codec.ChatMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.DisconnectMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.LoginMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.codec.LoginResponseMessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.ChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.DisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginResponseMessage;

public final class DefaultMessageRegistry {

    private DefaultMessageRegistry() {
    }

    public static MessageRegistry create() {
        MessageRegistry registry = new MessageRegistry();
        registry.register(PacketId.LOGIN, LoginMessage.class, new LoginMessageCodec());
        registry.register(PacketId.LOGIN_RESPONSE, LoginResponseMessage.class, new LoginResponseMessageCodec());
        registry.register(PacketId.CHAT, ChatMessage.class, new ChatMessageCodec());
        registry.register(PacketId.DISCONNECT, DisconnectMessage.class, new DisconnectMessageCodec());
        return registry;
    }
}