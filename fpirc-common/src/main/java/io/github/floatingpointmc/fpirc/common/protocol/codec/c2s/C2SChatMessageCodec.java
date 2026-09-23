package io.github.floatingpointmc.fpirc.common.protocol.codec.c2s;

import io.github.floatingpointmc.fpirc.common.protocol.MessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.common.protocol.StringCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SChatMessage;

import java.nio.ByteBuffer;

public final class C2SChatMessageCodec implements MessageCodec<C2SChatMessage> {

    public C2SChatMessageCodec() {
    }

    @Override
    public C2SChatMessage decode(ByteBuffer buffer) throws MessageDecodeException {
        String channel = StringCodec.readString(buffer);
        if (channel.isEmpty()) {
            throw new MessageDecodeException("Channel name must not be empty");
        }
        if (channel.length() > ProtocolConstants.MAX_CHANNEL_NAME_LENGTH) {
            throw new MessageDecodeException("Channel name too long: " + channel.length() + " > " + ProtocolConstants.MAX_CHANNEL_NAME_LENGTH);
        }
        String content = StringCodec.readString(buffer);
        if (content.length() > ProtocolConstants.MAX_MESSAGE_LENGTH) {
            throw new MessageDecodeException("Chat content too long: " + content.length() + " > " + ProtocolConstants.MAX_MESSAGE_LENGTH);
        }
        return new C2SChatMessage(channel, content);
    }

    @Override
    public void encode(C2SChatMessage message, ByteBuffer buffer) throws MessageEncodeException {
        String channel = message.getChannel();
        if (channel.isEmpty()) {
            throw new MessageEncodeException("Channel name must not be empty");
        }
        if (channel.length() > ProtocolConstants.MAX_CHANNEL_NAME_LENGTH) {
            throw new MessageEncodeException("Channel name too long: " + channel.length() + " > " + ProtocolConstants.MAX_CHANNEL_NAME_LENGTH);
        }
        StringCodec.writeString(buffer, channel);
        String content = message.getContent();
        if (content.length() > ProtocolConstants.MAX_MESSAGE_LENGTH) {
            throw new MessageEncodeException("Chat content too long: " + content.length() + " > " + ProtocolConstants.MAX_MESSAGE_LENGTH);
        }
        StringCodec.writeString(buffer, content);
    }
}