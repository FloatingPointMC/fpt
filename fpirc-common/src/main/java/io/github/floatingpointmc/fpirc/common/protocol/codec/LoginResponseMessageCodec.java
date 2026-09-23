package io.github.floatingpointmc.fpirc.common.protocol.codec;

import io.github.floatingpointmc.fpirc.common.protocol.MessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.common.protocol.StringCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginResponseMessage;

import java.nio.ByteBuffer;

public final class LoginResponseMessageCodec implements MessageCodec<LoginResponseMessage> {

    public LoginResponseMessageCodec() {
    }

    @Override
    public LoginResponseMessage decode(ByteBuffer buffer) throws MessageDecodeException {
        if (buffer.remaining() < 1) {
            throw new MessageDecodeException("Not enough bytes for LoginResponse success flag");
        }
        boolean success = buffer.get() != 0;
        String reason = StringCodec.readString(buffer);
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageDecodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        return new LoginResponseMessage(success, reason);
    }

    @Override
    public void encode(LoginResponseMessage message, ByteBuffer buffer) throws MessageEncodeException {
        buffer.put((byte) (message.isSuccess() ? 1 : 0));
        String reason = message.getReason();
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageEncodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        StringCodec.writeString(buffer, reason);
    }
}