package io.github.floatingpointmc.fpt.codec;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@UtilityClass
public final class VarInt {

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static int readVarInt(@NotNull ByteBuffer buffer) throws DecodeException {
        int value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            if (!buffer.hasRemaining()) {
                throw new DecodeException("Unexpected end of buffer while reading VarInt");
            }
            currentByte = buffer.get();
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) {
                break;
            }
            position += 7;
            if (position >= 35) {
                throw new DecodeException("VarInt is too big");
            }
        }
        return value;
    }

    public static void writeVarInt(@NotNull ByteBuffer buffer, int value) throws EncodeException {
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
        int size = 1;
        while ((value & ~SEGMENT_BITS) != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }
}