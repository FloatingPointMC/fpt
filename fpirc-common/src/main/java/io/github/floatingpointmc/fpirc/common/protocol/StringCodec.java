package io.github.floatingpointmc.fpirc.common.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class StringCodec {

    private StringCodec() {
    }

    public static String readString(ByteBuffer buffer) throws MessageDecodeException {
        int length = VarInt.readVarInt(buffer);
        if (length < 0) {
            throw new MessageDecodeException("Negative string length: " + length);
        }
        if (length > ProtocolConstants.MAX_STRING_LENGTH) {
            throw new MessageDecodeException("String too long: " + length + " > " + ProtocolConstants.MAX_STRING_LENGTH);
        }
        if (buffer.remaining() < length) {
            throw new MessageDecodeException("Not enough bytes for string: need " + length + ", have " + buffer.remaining());
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuffer buffer, String value) throws MessageEncodeException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > ProtocolConstants.MAX_STRING_LENGTH) {
            throw new MessageEncodeException("String too long: " + bytes.length + " > " + ProtocolConstants.MAX_STRING_LENGTH);
        }
        VarInt.writeVarInt(buffer, bytes.length);
        buffer.put(bytes);
    }

    public static int stringSize(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return VarInt.varIntSize(bytes.length) + bytes.length;
    }
}