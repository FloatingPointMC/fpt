package io.github.floatingpointmc.fpirc.common.protocol.codec.c2s;

import io.github.floatingpointmc.fpirc.common.protocol.MessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.common.protocol.StringCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SLoginMessage;

import java.nio.ByteBuffer;

public final class C2SLoginMessageCodec implements MessageCodec<C2SLoginMessage> {

    public C2SLoginMessageCodec() {
    }

    @Override
    public C2SLoginMessage decode(ByteBuffer buffer) throws MessageDecodeException {
        String username = StringCodec.readString(buffer);
        if (username.isEmpty()) {
            throw new MessageDecodeException("Username must not be empty");
        }
        if (username.length() > ProtocolConstants.MAX_USERNAME_LENGTH) {
            throw new MessageDecodeException("Username too long: " + username.length() + " > " + ProtocolConstants.MAX_USERNAME_LENGTH);
        }
        return new C2SLoginMessage(username);
    }

    @Override
    public void encode(C2SLoginMessage message, ByteBuffer buffer) throws MessageEncodeException {
        String username = message.getUsername();
        if (username.isEmpty()) {
            throw new MessageEncodeException("Username must not be empty");
        }
        if (username.length() > ProtocolConstants.MAX_USERNAME_LENGTH) {
            throw new MessageEncodeException("Username too long: " + username.length() + " > " + ProtocolConstants.MAX_USERNAME_LENGTH);
        }
        StringCodec.writeString(buffer, username);
    }
}