package io.github.floatingpointmc.fpirc.common.protocol;

import java.nio.ByteBuffer;

public final class VarInt {

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;
    private static final int MAX_BYTES = 5;

    private VarInt() {
    }

    public static int readVarInt(ByteBuffer buffer) throws MessageDecodeException {
        int value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            if (!buffer.hasRemaining()) {
                throw new MessageDecodeException("Unexpected end of buffer while reading VarInt");
            }
            currentByte = buffer.get();
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) {
                break;
            }
            position += 7;
            if (position >= 32) {
                throw new MessageDecodeException("VarInt is too big");
            }
        }
        return value;
    }

    public static void writeVarInt(ByteBuffer buffer, int value) throws MessageEncodeException {
        if (value < 0) {
            throw new MessageEncodeException("VarInt does not support negative values: " + value);
        }
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                buffer.put((byte) value);
                return;
            }
            buffer.put((byte) ((value & SEGMENT_BITS) | CONTINUE_BIT));
            value >>>= 7;
        }
    }

    public static int varIntSize(int value) {
        if (value < 0) {
            return MAX_BYTES;
        }
        int size = 1;
        while ((value & ~SEGMENT_BITS) != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }

    public static int readVarInt(byte[] data, int offset) throws MessageDecodeException {
        int value = 0;
        int position = 0;
        int index = offset;
        while (true) {
            if (index >= data.length) {
                throw new MessageDecodeException("Unexpected end of data while reading VarInt");
            }
            byte currentByte = data[index++];
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) {
                break;
            }
            position += 7;
            if (position >= 32) {
                throw new MessageDecodeException("VarInt is too big");
            }
        }
        return value;
    }

    public static int writeVarInt(byte[] data, int offset, int value) throws MessageEncodeException {
        if (value < 0) {
            throw new MessageEncodeException("VarInt does not support negative values: " + value);
        }
        int index = offset;
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                data[index++] = (byte) value;
                return index - offset;
            }
            data[index++] = (byte) ((value & SEGMENT_BITS) | CONTINUE_BIT);
            value >>>= 7;
        }
    }
}