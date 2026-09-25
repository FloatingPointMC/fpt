package io.github.floatingpointmc.fpt.codec;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@UtilityClass
public final class VarLong {

    private static final long SEGMENT_BITS = 0x7FL;
    private static final long CONTINUE_BIT = 0x80L;

    public static long readVarLong(@NotNull ByteBuffer buffer) throws DecodeException {
        long value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            if (!buffer.hasRemaining()) {
                throw new DecodeException("Unexpected end of buffer while reading VarLong");
            }
            currentByte = buffer.get();
            value |= (currentByte & SEGMENT_BITS) << position;
            if ((currentByte & CONTINUE_BIT) == 0) {
                break;
            }
            position += 7;
            if (position >= 70) {
                throw new DecodeException("VarLong is too big");
            }
        }
        return value;
    }

    public static void writeVarLong(@NotNull ByteBuffer buffer, long value) throws EncodeException {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                buffer.put((byte) value);
                return;
            }
            buffer.put((byte) ((value & SEGMENT_BITS) | CONTINUE_BIT));
            value >>>= 7;
        }
    }

    public static int varLongSize(long value) {
        int size = 1;
        while ((value & ~SEGMENT_BITS) != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }
}