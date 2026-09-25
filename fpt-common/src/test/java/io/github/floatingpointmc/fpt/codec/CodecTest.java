package io.github.floatingpointmc.fpt.codec;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CodecTest {

    @Test
    void defaultCodecIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Codec.DEFAULT_CODEC.put(Integer.class, Codec.integerCodec());
        });
    }

    @Test
    void booleanCodec() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Boolean> codec = cast(Codec.booleanCodec());
        codec.encode(buf, true);
        codec.encode(buf, false);
        buf.flip();
        assertTrue(codec.decode(buf));
        assertFalse(codec.decode(buf));
    }

    @Test
    void byteCodec() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Byte> codec = cast(Codec.byteCodec());
        codec.encode(buf, (byte) 42);
        codec.encode(buf, (byte) -1);
        buf.flip();
        assertEquals((byte) 42, codec.decode(buf).byteValue());
        assertEquals((byte) -1, codec.decode(buf).byteValue());
    }

    @Test
    void shortCodec() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Short> codec = cast(Codec.shortCodec());
        codec.encode(buf, (short) 1000);
        codec.encode(buf, (short) -500);
        buf.flip();
        assertEquals((short) 1000, codec.decode(buf).shortValue());
        assertEquals((short) -500, codec.decode(buf).shortValue());
    }

    @Test
    void integerVarInt() throws Exception {
        int[] values = {0, 1, -1, 127, 128, 255, 256, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int val : values) {
            ByteBuffer buf = ByteBuffer.allocate(32);
            Codec<Integer> codec = cast(Codec.integerCodec());
            codec.encode(buf, val);
            buf.flip();
            int decoded = codec.decode(buf);
            assertEquals(val, decoded, "VarInt roundtrip for " + val);
        }
    }

    @Test
    void longVarLong() throws Exception {
        long[] values = {0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE};
        for (long val : values) {
            ByteBuffer buf = ByteBuffer.allocate(32);
            Codec<Long> codec = cast(Codec.longCodec());
            codec.encode(buf, val);
            buf.flip();
            long decoded = codec.decode(buf);
            assertEquals(val, decoded, "VarLong roundtrip for " + val);
        }
    }

    @Test
    void floatCodec() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Float> codec = cast(Codec.floatCodec());
        codec.encode(buf, 3.14f);
        buf.flip();
        assertEquals(3.14f, codec.decode(buf), 0.0f);
    }

    @Test
    void doubleCodec() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Double> codec = cast(Codec.doubleCodec());
        codec.encode(buf, 2.718281828);
        buf.flip();
        assertEquals(2.718281828, codec.decode(buf), 0.0);
    }

    @Test
    void characterCodec() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Character> codec = cast(Codec.characterCodec());
        codec.encode(buf, 'A');
        buf.flip();
        assertEquals('A', codec.decode(buf).charValue());
    }

    @Test
    void stringUtf8() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append('a');
        String longString = sb.toString();
        String[] values = {"", "hello", "你好世界", "🎉🚀", longString};
        for (String val : values) {
            ByteBuffer buf = ByteBuffer.allocate(val.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + 16);
            Codec<String> codec = cast(Codec.stringCodec());
            codec.encode(buf, val);
            buf.flip();
            assertEquals(val, codec.decode(buf), "String roundtrip for length=" + val.length());
        }
    }

    @Test
    void uuid128() throws Exception {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buf = ByteBuffer.allocate(32);
        Codec<UUID> codec = cast(Codec.uuidCodec());
        codec.encode(buf, uuid);
        buf.flip();
        UUID decoded = codec.decode(buf);
        assertEquals(uuid, decoded);
        assertEquals(uuid.getMostSignificantBits(), decoded.getMostSignificantBits());
        assertEquals(uuid.getLeastSignificantBits(), decoded.getLeastSignificantBits());
    }

    @Test
    void byteArrayCodec() throws Exception {
        byte[] arr = new byte[]{1, 2, 3, 4, 5};
        ByteBuffer buf = ByteBuffer.allocate(32);
        Codec<byte[]> codec = cast(Codec.byteArrayCodec());
        codec.encode(buf, arr);
        buf.flip();
        byte[] decoded = codec.decode(buf);
        assertArrayEquals(arr, decoded);
    }

    @Test
    void fixed32Codec() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Integer> codec = Fixed32Codec.INSTANCE;
        codec.encode(buf, 42);
        codec.encode(buf, -1);
        buf.flip();
        assertEquals(42, codec.decode(buf).intValue());
        assertEquals(-1, codec.decode(buf).intValue());
    }

    @Test
    void varIntAndFixed32AreDifferentIdentity() {
        assertNotEquals(Codec.integerCodec().identity(), Fixed32Codec.INSTANCE.identity());
    }

    @Test
    void boxPrimitive() {
        assertEquals(Integer.class, Codec.box(int.class));
        assertEquals(Long.class, Codec.box(long.class));
        assertEquals(Boolean.class, Codec.box(boolean.class));
        assertEquals(Byte.class, Codec.box(byte.class));
        assertEquals(Short.class, Codec.box(short.class));
        assertEquals(Float.class, Codec.box(float.class));
        assertEquals(Double.class, Codec.box(double.class));
        assertEquals(Character.class, Codec.box(char.class));
        assertEquals(String.class, Codec.box(String.class));
    }

    @SuppressWarnings("unchecked")
    private <T> Codec<T> cast(Codec<?> codec) {
        return (Codec<T>) codec;
    }
}