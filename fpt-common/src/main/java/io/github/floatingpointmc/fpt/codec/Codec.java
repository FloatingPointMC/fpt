package io.github.floatingpointmc.fpt.codec;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
        public void encode(ByteBuffer buf, Boolean value) {
            buf.put(value ? (byte) 1 : (byte) 0);
        }

        @Override
        public Boolean decode(ByteBuffer buf) {
            return buf.get() != 0;
        }
    };

    private static final Codec<Byte> BYTE = new Codec<Byte>("fpt:byte") {
        @Override
        public void encode(ByteBuffer buf, Byte value) {
            buf.put(value);
        }

        @Override
        public Byte decode(ByteBuffer buf) {
            return buf.get();
        }
    };

    private static final Codec<Short> SHORT = new Codec<Short>("fpt:short") {
        @Override
        public void encode(ByteBuffer buf, Short value) {
            buf.putShort(value);
        }

        @Override
        public Short decode(ByteBuffer buf) {
            return buf.getShort();
        }
    };

    private static final Codec<Integer> INTEGER = new Codec<Integer>("fpt:int:varint32") {
        @Override
        public void encode(ByteBuffer buf, Integer value) throws EncodeException {
            VarInt.writeVarInt(buf, value);
        }

        @Override
        public Integer decode(ByteBuffer buf) throws DecodeException {
            return VarInt.readVarInt(buf);
        }
    };

    private static final Codec<Long> LONG = new Codec<Long>("fpt:long:varlong64") {
        @Override
        public void encode(ByteBuffer buf, Long value) throws EncodeException {
            VarLong.writeVarLong(buf, value);
        }

        @Override
        public Long decode(ByteBuffer buf) throws DecodeException {
            return VarLong.readVarLong(buf);
        }
    };

    private static final Codec<Float> FLOAT = new Codec<Float>("fpt:float") {
        @Override
        public void encode(ByteBuffer buf, Float value) {
            buf.putFloat(value);
        }

        @Override
        public Float decode(ByteBuffer buf) {
            return buf.getFloat();
        }
    };

    private static final Codec<Double> DOUBLE = new Codec<Double>("fpt:double") {
        @Override
        public void encode(ByteBuffer buf, Double value) {
            buf.putDouble(value);
        }

        @Override
        public Double decode(ByteBuffer buf) {
            return buf.getDouble();
        }
    };

    private static final Codec<Character> CHARACTER = new Codec<Character>("fpt:char") {
        @Override
        public void encode(ByteBuffer buf, Character value) {
            buf.putChar(value);
        }

        @Override
        public Character decode(ByteBuffer buf) {
            return buf.getChar();
        }
    };

    private static final Codec<String> STRING = new Codec<String>("fpt:string:utf8") {
        @Override
        public void encode(ByteBuffer buf, String value) throws EncodeException {
            byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            if (bytes.length > 32768) {
                throw new EncodeException("String too long: " + bytes.length);
            }
            VarInt.writeVarInt(buf, bytes.length);
            buf.put(bytes);
        }

        @Override
        public String decode(ByteBuffer buf) throws DecodeException {
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

    private static final Codec<java.util.UUID> UUID = new Codec<java.util.UUID>("fpt:uuid:128") {
        @Override
        public void encode(ByteBuffer buf, java.util.UUID value) {
            buf.putLong(value.getMostSignificantBits());
            buf.putLong(value.getLeastSignificantBits());
        }

        @Override
        public java.util.UUID decode(ByteBuffer buf) {
            long msb = buf.getLong();
            long lsb = buf.getLong();
            return new java.util.UUID(msb, lsb);
        }
    };

    private static final Codec<byte[]> BYTE_ARRAY = new Codec<byte[]>("fpt:bytes") {
        @Override
        public void encode(ByteBuffer buf, byte[] value) throws EncodeException {
            if (value.length > 32768) {
                throw new EncodeException("Byte array too long: " + value.length);
            }
            VarInt.writeVarInt(buf, value.length);
            buf.put(value);
        }

        @Override
        public byte[] decode(ByteBuffer buf) throws DecodeException {
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

    public static final Map<Class<?>, Codec<?>> DEFAULT_CODEC;

    static {
        Map<Class<?>, Codec<?>> map = new LinkedHashMap<>();
        map.put(Boolean.class, BOOLEAN);
        map.put(boolean.class, BOOLEAN);
        map.put(Byte.class, BYTE);
        map.put(byte.class, BYTE);
        map.put(Short.class, SHORT);
        map.put(short.class, SHORT);
        map.put(Integer.class, INTEGER);
        map.put(int.class, INTEGER);
        map.put(Long.class, LONG);
        map.put(long.class, LONG);
        map.put(Float.class, FLOAT);
        map.put(float.class, FLOAT);
        map.put(Double.class, DOUBLE);
        map.put(double.class, DOUBLE);
        map.put(Character.class, CHARACTER);
        map.put(char.class, CHARACTER);
        map.put(String.class, STRING);
        map.put(java.util.UUID.class, UUID);
        map.put(byte[].class, BYTE_ARRAY);
        DEFAULT_CODEC = Collections.unmodifiableMap(map);
    }

    public static @NotNull Codec<?> booleanCodec() {
        return BOOLEAN;
    }

    public static @NotNull Codec<?> byteCodec() {
        return BYTE;
    }

    public static @NotNull Codec<?> shortCodec() {
        return SHORT;
    }

    public static @NotNull Codec<?> integerCodec() {
        return INTEGER;
    }

    public static @NotNull Codec<?> longCodec() {
        return LONG;
    }

    public static @NotNull Codec<?> floatCodec() {
        return FLOAT;
    }

    public static @NotNull Codec<?> doubleCodec() {
        return DOUBLE;
    }

    public static @NotNull Codec<?> characterCodec() {
        return CHARACTER;
    }

    public static @NotNull Codec<?> stringCodec() {
        return STRING;
    }

    public static @NotNull Codec<?> uuidCodec() {
        return UUID;
    }

    public static @NotNull Codec<?> byteArrayCodec() {
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