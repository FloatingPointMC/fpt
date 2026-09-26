package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.codec.exceptions.DecodeException;
import io.github.floatingpointmc.fpt.codec.exceptions.EncodeException;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class Codec<T> {
    private final @NotNull String identity;

    protected Codec(@NotNull String identity) {
        this.identity = identity;
    }

    public final @NotNull String identity() {
        return identity;
    }

    public abstract void encode(@NotNull ByteBuffer buf, T value) throws EncodeException;

    public abstract T decode(@NotNull ByteBuffer buf) throws DecodeException;

    @Override
    public final @NotNull String toString() {
        return identity;
    }

    private static final Codec<Boolean> BOOLEAN = new Codec<Boolean>("fpt:boolean") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Boolean value) {
            buf.put(value ? (byte) 1 : (byte) 0);
        }

        @Override
        public Boolean decode(@NotNull ByteBuffer buf) {
            return buf.get() != 0;
        }
    };

    private static final Codec<Byte> BYTE = new Codec<Byte>("fpt:byte") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Byte value) {
            buf.put(value);
        }

        @Override
        public Byte decode(@NotNull ByteBuffer buf) {
            return buf.get();
        }
    };

    private static final Codec<Short> SHORT = new Codec<Short>("fpt:short") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Short value) {
            buf.putShort(value);
        }

        @Override
        public Short decode(@NotNull ByteBuffer buf) {
            return buf.getShort();
        }
    };

    private static final Codec<Integer> INTEGER = new Codec<Integer>("fpt:int:varint32") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Integer value) {
            VarInt.writeVarInt(buf, value);
        }

        @Override
        public Integer decode(@NotNull ByteBuffer buf) throws DecodeException {
            return VarInt.readVarInt(buf);
        }
    };

    private static final Codec<Long> LONG = new Codec<Long>("fpt:long:varlong64") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Long value) {
            VarLong.writeVarLong(buf, value);
        }

        @Override
        public Long decode(@NotNull ByteBuffer buf) throws DecodeException {
            return VarLong.readVarLong(buf);
        }
    };

    private static final Codec<Float> FLOAT = new Codec<Float>("fpt:float") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Float value) {
            buf.putFloat(value);
        }

        @Override
        public Float decode(@NotNull ByteBuffer buf) {
            return buf.getFloat();
        }
    };

    private static final Codec<Double> DOUBLE = new Codec<Double>("fpt:double") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Double value) {
            buf.putDouble(value);
        }

        @Override
        public Double decode(@NotNull ByteBuffer buf) {
            return buf.getDouble();
        }
    };

    private static final Codec<Character> CHARACTER = new Codec<Character>("fpt:char") {
        @Override
        public void encode(@NotNull ByteBuffer buf, Character value) {
            buf.putChar(value);
        }

        @Override
        public Character decode(@NotNull ByteBuffer buf) {
            return buf.getChar();
        }
    };

    private static final Codec<String> STRING = new Codec<String>("fpt:string:utf8") {
        @Override
        public void encode(@NotNull ByteBuffer buf, String value) throws EncodeException {
            byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            if (bytes.length > 32768) {
                throw new EncodeException("String too long: " + bytes.length);
            }
            VarInt.writeVarInt(buf, bytes.length);
            buf.put(bytes);
        }

        @Override
        public String decode(@NotNull ByteBuffer buf) throws DecodeException {
            int length = VarInt.readVarInt(buf);
            if (length < 0) {
                throw new DecodeException("Negative string length: " + length);
            }
            if (length > 32768) {
                throw new DecodeException("String too long: " + length);
            }
            if (buf.remaining() < length) {
                throw new DecodeException("Not enough bytes for string: need " + length + ", have " + buf.remaining());
            }
            byte[] bytes = new byte[length];
            buf.get(bytes);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }
    };

    private static final Codec<UUID> UUID = new Codec<UUID>("fpt:uuid:128") {
        @Override
        public void encode(@NotNull ByteBuffer buf, UUID value) {
            buf.putLong(value.getMostSignificantBits());
            buf.putLong(value.getLeastSignificantBits());
        }

        @Override
        public UUID decode(@NotNull ByteBuffer buf) {
            long msb = buf.getLong();
            long lsb = buf.getLong();
            return new UUID(msb, lsb);
        }
    };

    private static final Codec<byte[]> BYTE_ARRAY = new Codec<byte[]>("fpt:bytes") {
        @Override
        public void encode(@NotNull ByteBuffer buf, byte[] value) throws EncodeException {
            if (value.length > 32768) {
                throw new EncodeException("Byte array too long: " + value.length);
            }
            VarInt.writeVarInt(buf, value.length);
            buf.put(value);
        }

        @Override
        public byte[] decode(@NotNull ByteBuffer buf) throws DecodeException {
            int length = VarInt.readVarInt(buf);
            if (length < 0) {
                throw new DecodeException("Negative byte array length: " + length);
            }
            if (length > 32768) {
                throw new DecodeException("Byte array too long: " + length);
            }
            if (buf.remaining() < length) {
                throw new DecodeException("Not enough bytes for byte array: need " + length + ", have " + buf.remaining());
            }
            byte[] bytes = new byte[length];
            buf.get(bytes);
            return bytes;
        }
    };

    private static final CodecMap DEFAULT_CODEC;

    static {
        DEFAULT_CODEC = CodecMap.create()
                .put(Boolean.class, BOOLEAN)
                .put(boolean.class, BOOLEAN)
                .put(Byte.class, BYTE)
                .put(byte.class, BYTE)
                .put(Short.class, SHORT)
                .put(short.class, SHORT)
                .put(Integer.class, INTEGER)
                .put(int.class, INTEGER)
                .put(Long.class, LONG)
                .put(long.class, LONG)
                .put(Float.class, FLOAT)
                .put(float.class, FLOAT)
                .put(Double.class, DOUBLE)
                .put(double.class, DOUBLE)
                .put(Character.class, CHARACTER)
                .put(char.class, CHARACTER)
                .put(String.class, STRING)
                .put(java.util.UUID.class, UUID)
                .put(byte[].class, BYTE_ARRAY);
    }

    public static CodecMap defaultCodecMap() {
        return new CodecMap(DEFAULT_CODEC);
    }

    public static @NotNull Codec<Boolean> booleanCodec() {
        return BOOLEAN;
    }

    public static @NotNull Codec<Byte> byteCodec() {
        return BYTE;
    }

    public static @NotNull Codec<Short> shortCodec() {
        return SHORT;
    }

    public static @NotNull Codec<Integer> integerCodec() {
        return INTEGER;
    }

    public static @NotNull Codec<Long> longCodec() {
        return LONG;
    }

    public static @NotNull Codec<Float> floatCodec() {
        return FLOAT;
    }

    public static @NotNull Codec<Double> doubleCodec() {
        return DOUBLE;
    }

    public static @NotNull Codec<Character> characterCodec() {
        return CHARACTER;
    }

    public static @NotNull Codec<String> stringCodec() {
        return STRING;
    }

    public static @NotNull Codec<UUID> uuidCodec() {
        return UUID;
    }

    public static @NotNull Codec<byte[]> byteArrayCodec() {
        return BYTE_ARRAY;
    }

    public static @NotNull Class<?> box(@NotNull Class<?> type) {
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }
}