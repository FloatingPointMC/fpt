package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.codec.exceptions.DecodeException;
import io.github.floatingpointmc.fpt.codec.exceptions.EncodeException;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.protocol.MessageDirection;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.ProtocolConstants;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@UtilityClass
public final class MessageCoder {
    private static final int INITIAL_PAYLOAD_CAPACITY = 256;

    public static @NotNull Message decode(@NotNull Protocol protocol, @NotNull MessageDirection direction, @NotNull ByteBuffer payload) throws DecodeException {
        int messageId = VarInt.readVarInt(payload);
        MessageCodec<?> codec = protocol.getMessageCodec(messageId, direction);
        if (codec == null) {
            throw new DecodeException("Unknown message ID: 0x" + Integer.toHexString(messageId) + " direction=" + direction);
        }
        return decodeWithCodec(codec, payload);
    }

    private static <T extends Message> T decodeWithCodec(MessageCodec<T> codec, ByteBuffer payload) throws DecodeException {
        return codec.decode(payload);
    }

    @SuppressWarnings("unchecked")
    public static byte[] encode(@NotNull Protocol protocol, @NotNull Message message) throws EncodeException {
        int messageId = protocol.getMessageId(message.getClass());
        MessageCodec<Message> codec = (MessageCodec<Message>) protocol.getMessageCodec(message.getClass());

        ByteBuffer payloadBuffer = encodePayload(messageId, codec, message);
        payloadBuffer.flip();
        int payloadLength = payloadBuffer.remaining();

        if (payloadLength > ProtocolConstants.MAX_PACKET_SIZE) {
            throw new EncodeException("Packet too large: " + payloadLength + " > " + ProtocolConstants.MAX_PACKET_SIZE);
        }

        int lengthSize = VarInt.varIntSize(payloadLength);
        byte[] packet = new byte[lengthSize + payloadLength];

        writeVarIntToArray(packet, payloadLength);
        payloadBuffer.get(packet, lengthSize, payloadLength);

        return packet;
    }

    private static <T extends Message> ByteBuffer encodePayload(int messageId, MessageCodec<T> codec, T message)
            throws EncodeException {
        int packetIdSize = VarInt.varIntSize(messageId);
        int capacity = packetIdSize + INITIAL_PAYLOAD_CAPACITY;

        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(capacity);
            try {
                VarInt.writeVarInt(buffer, messageId);
                codec.encode(buffer, message);
                return buffer;
            } catch (java.nio.BufferOverflowException e) {
                capacity *= 2;
                if (capacity > ProtocolConstants.MAX_PACKET_SIZE + packetIdSize) {
                    throw new EncodeException("Message payload exceeds maximum packet size", e);
                }
            }
        }
    }

    private static void writeVarIntToArray(byte[] data, int value) {
        int encoded = (value << 1) ^ (value >> 31);
        int index = 0;
        while (true) {
            if ((encoded & ~0x7F) == 0) {
                data[index] = (byte) encoded;
                return;
            }
            data[index] = (byte) ((encoded & 0x7F) | 0x80);
            encoded >>>= 7;
            index++;
        }
    }
}