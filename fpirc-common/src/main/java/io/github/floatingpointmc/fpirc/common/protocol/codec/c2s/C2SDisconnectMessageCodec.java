package io.github.floatingpointmc.fpirc.common.protocol.codec.c2s;

import io.github.floatingpointmc.fpirc.common.protocol.MessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.common.protocol.StringCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SDisconnectMessage;

import java.nio.ByteBuffer;

public final class C2SDisconnectMessageCodec implements MessageCodec<C2SDisconnectMessage> {

    public C2SDisconnectMessageCodec() {
    }

    @Override
    public C2SDisconnectMessage decode(ByteBuffer buffer) throws MessageDecodeException {
        String reason = StringCodec.readString(buffer);
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageDecodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        return new C2SDisconnectMessage(reason);
    }

    @Override
    public void encode(C2SDisconnectMessage message, ByteBuffer buffer) throws MessageEncodeException {
        String reason = message.getReason();
        if (reason.length() > ProtocolConstants.MAX_REASON_LENGTH) {
            throw new MessageEncodeException("Reason too long: " + reason.length() + " > " + ProtocolConstants.MAX_REASON_LENGTH);
        }
        StringCodec.writeString(buffer, reason);
    }
}