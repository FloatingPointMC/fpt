package io.github.floatingpointmc.fpirc.common.protocol.codec.s2c;

import io.github.floatingpointmc.fpirc.common.protocol.MessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.common.protocol.StringCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CDisconnectMessage;

import java.nio.ByteBuffer;

public final class S2CDisconnectMessageCodec implements MessageCodec<S2CDisconnectMessage> {

    public S2CDisconnectMessageCodec() {
    }

    @Override
    public S2CDisconnectMessage decode(ByteBuffer buffer) throws MessageDecodeException {
        String reason = StringCodec.readString(buffer);
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageDecodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        return new S2CDisconnectMessage(reason);
    }

    @Override
    public void encode(S2CDisconnectMessage message, ByteBuffer buffer) throws MessageEncodeException {
        String reason = message.getReason();
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageEncodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        StringCodec.writeString(buffer, reason);
    }
}