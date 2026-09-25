package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.codec.exceptions.DecodeException;
import io.github.floatingpointmc.fpt.codec.exceptions.EncodeException;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@UtilityClass
public final class VarLong {

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static long readVarLong(@NotNull ByteBuffer buffer) throws DecodeException {
        long value = 0;
        int position = 0;

        while (true) {
            if (!buffer.hasRemaining()) {
                throw new DecodeException(
                        "Unexpected end of buffer while reading VarLong"
                );
            }

            int currentByte = buffer.get() & 0xFF;

            value |= (long) (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) {
                return decodeZigZag(value);
            }

            position += 7;

            if (position >= 70) {
                throw new DecodeException("VarLong is too big");
            }
        }
    }

    public static void writeVarLong(
            @NotNull ByteBuffer buffer,
            long value
    ) {
        long encoded = encodeZigZag(value);

        while ((encoded & ~SEGMENT_BITS) != 0) {
            buffer.put((byte) ((encoded & SEGMENT_BITS) | CONTINUE_BIT));
            encoded >>>= 7;
        }

        buffer.put((byte) encoded);
    }

    public static int varLongSize(long value) {
        long encoded = encodeZigZag(value);

        int size = 1;

        while ((encoded & ~SEGMENT_BITS) != 0) {
            size++;
            encoded >>>= 7;
        }

        return size;
    }

    private static long encodeZigZag(long value) {
        return (value << 1) ^ (value >> 63);
    }

    private static long decodeZigZag(long value) {
        return (value >>> 1) ^ -(value & 1);
    }
}