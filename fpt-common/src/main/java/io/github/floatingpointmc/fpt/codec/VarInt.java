package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.codec.exceptions.DecodeException;
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

        while (true) {
            if (!buffer.hasRemaining()) {
                throw new DecodeException(
                        "Unexpected end of buffer while reading VarInt"
                );
            }

            int currentByte = buffer.get() & 0xFF;

            value |= (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) {
                return decodeZigZag(value);
            }

            position += 7;

            if (position >= 35) {
                throw new DecodeException("VarInt is too big");
            }
        }
    }

    public static void writeVarInt(
            @NotNull ByteBuffer buffer,
            int value
    ) {
        int encoded = encodeZigZag(value);

        while ((encoded & ~SEGMENT_BITS) != 0) {
            buffer.put((byte) ((encoded & SEGMENT_BITS) | CONTINUE_BIT));
            encoded >>>= 7;
        }
        buffer.put((byte) encoded);
    }

    public static int varIntSize(int value) {
        int encoded = encodeZigZag(value);

        int size = 1;

        while ((encoded & ~SEGMENT_BITS) != 0) {
            size++;
            encoded >>>= 7;
        }

        return size;
    }

    private static int encodeZigZag(int value) {
        return (value << 1) ^ (value >> 31);
    }

    private static int decodeZigZag(int value) {
        return (value >>> 1) ^ -(value & 1);
    }
}