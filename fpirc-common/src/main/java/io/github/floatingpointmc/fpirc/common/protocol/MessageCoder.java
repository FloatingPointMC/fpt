package io.github.floatingpointmc.fpirc.common.protocol;

import java.nio.ByteBuffer;

public final class MessageCoder {

    private static final int INITIAL_PAYLOAD_CAPACITY = 256;

    private MessageCoder() {
    }

    public static Message decode(MessageRegistry registry, ByteBuffer payload) throws MessageDecodeException {
        int packetId = VarInt.readVarInt(payload);
        if (!registry.isRegistered(packetId)) {
            throw new MessageDecodeException("Unknown packet ID: 0x" + Integer.toHexString(packetId));
        }
        MessageCodec<? extends Message> codec = registry.getCodec(packetId);
        return decodeWithCodec(codec, payload);
    }

    private static <T extends Message> T decodeWithCodec(MessageCodec<T> codec, ByteBuffer payload)
            throws MessageDecodeException {
        return codec.decode(payload);
    }

    @SuppressWarnings("unchecked")
    public static byte[] encode(MessageRegistry registry, Message message) throws MessageEncodeException {
        int packetId = registry.getPacketId(message.getClass());
        MessageCodec<Message> codec = (MessageCodec<Message>) registry.getCodec(message.getClass());

        ByteBuffer payloadBuffer = encodePayload(packetId, codec, message);
        payloadBuffer.flip();
        int payloadLength = payloadBuffer.remaining();

        if (payloadLength > ProtocolConstants.MAX_PACKET_SIZE) {
            throw new MessageEncodeException("Packet too large: " + payloadLength + " > " + ProtocolConstants.MAX_PACKET_SIZE);
        }

        int lengthSize = VarInt.varIntSize(payloadLength);
        byte[] packet = new byte[lengthSize + payloadLength];

        VarInt.writeVarInt(packet, 0, payloadLength);
        payloadBuffer.get(packet, lengthSize, payloadLength);

        return packet;
    }

    private static <T extends Message> ByteBuffer encodePayload(int packetId, MessageCodec<T> codec, T message)
            throws MessageEncodeException {
        int packetIdSize = VarInt.varIntSize(packetId);
        int capacity = packetIdSize + INITIAL_PAYLOAD_CAPACITY;

        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(capacity);
            try {
                VarInt.writeVarInt(buffer, packetId);
                codec.encode(message, buffer);
                return buffer;
            } catch (java.nio.BufferOverflowException e) {
                capacity *= 2;
                if (capacity > ProtocolConstants.MAX_PACKET_SIZE + packetIdSize) {
                    throw new MessageEncodeException("Message payload exceeds maximum packet size", e);
                }
            }
        }
    }
}