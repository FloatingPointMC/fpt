package io.github.floatingpointmc.fpirc.common.protocol.codec;

import io.github.floatingpointmc.fpirc.common.protocol.MessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.common.protocol.StringCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.DisconnectMessage;

import java.nio.ByteBuffer;

public final class DisconnectMessageCodec implements MessageCodec<DisconnectMessage> {

    public DisconnectMessageCodec() {
    }

    @Override
    public DisconnectMessage decode(ByteBuffer buffer) throws MessageDecodeException {
        String reason = StringCodec.readString(buffer);
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageDecodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        return new DisconnectMessage(reason);
    }

    @Override
    public void encode(DisconnectMessage message, ByteBuffer buffer) throws MessageEncodeException {
        String reason = message.getReason();
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageEncodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        StringCodec.writeString(buffer, reason);
    }
}