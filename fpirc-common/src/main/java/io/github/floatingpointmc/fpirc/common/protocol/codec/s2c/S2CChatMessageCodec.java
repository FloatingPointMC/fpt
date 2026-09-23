package io.github.floatingpointmc.fpirc.common.protocol.codec.s2c;

import io.github.floatingpointmc.fpirc.common.protocol.MessageCodec;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.ProtocolConstants;
import io.github.floatingpointmc.fpirc.common.protocol.StringCodec;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage;

import java.nio.ByteBuffer;

public final class S2CChatMessageCodec implements MessageCodec<S2CChatMessage> {

    public S2CChatMessageCodec() {
    }

    @Override
    public S2CChatMessage decode(ByteBuffer buffer) throws MessageDecodeException {
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
        return new S2CChatMessage(channel, content);
    }

    @Override
    public void encode(S2CChatMessage message, ByteBuffer buffer) throws MessageEncodeException {
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