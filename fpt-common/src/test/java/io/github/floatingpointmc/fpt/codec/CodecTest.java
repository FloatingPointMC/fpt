package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.codec.exceptions.DecodeException;
import io.github.floatingpointmc.fpt.codec.exceptions.EncodeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CodecTest {

    @Test
    void booleanRoundtrip() throws EncodeException, DecodeException {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Boolean> codec = Codec.booleanCodec();
        codec.encode(buf, true);
        codec.encode(buf, false);
        buf.flip();
        assertTrue(codec.decode(buf));
        assertFalse(codec.decode(buf));
    }

    @Test
    void byteRoundtrip() throws EncodeException, DecodeException {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Byte> codec = Codec.byteCodec();
        codec.encode(buf, (byte) 42);
        codec.encode(buf, (byte) -1);
        buf.flip();
        assertEquals((byte) 42, codec.decode(buf).byteValue());
        assertEquals((byte) -1, codec.decode(buf).byteValue());
    }

    @Test
    void shortRoundtrip() throws EncodeException, DecodeException {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Short> codec = Codec.shortCodec();
        codec.encode(buf, (short) 1000);
        codec.encode(buf, (short) -500);
        buf.flip();
        assertEquals((short) 1000, codec.decode(buf).shortValue());
        assertEquals((short) -500, codec.decode(buf).shortValue());
    }

    @Test
    void integerVarIntRoundtrip() throws EncodeException, DecodeException {
        int[] values = {0, 1, -1, 127, 128, 255, 256, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int val : values) {
            ByteBuffer buf = ByteBuffer.allocate(32);
            Codec<Integer> codec = Codec.integerCodec();
            codec.encode(buf, val);
            buf.flip();
            assertEquals(val, codec.decode(buf).intValue(), "VarInt roundtrip for " + val);
        }
    }

    @Test
    void longVarLongRoundtrip() throws EncodeException, DecodeException {
        long[] values = {0L, 1L, -1L, 127L, 128L, Long.MAX_VALUE, Long.MIN_VALUE};
        for (long val : values) {
            ByteBuffer buf = ByteBuffer.allocate(80);
            Codec<Long> codec = Codec.longCodec();
            codec.encode(buf, val);
            buf.flip();
            assertEquals(val, codec.decode(buf).longValue(), "VarLong roundtrip for " + val);
        }
    }

    @Test
    void floatRoundtrip() throws EncodeException, DecodeException {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Float> codec = Codec.floatCodec();
        codec.encode(buf, 3.14f);
        buf.flip();
        assertEquals(3.14f, codec.decode(buf), 0.0f);
    }

    @Test
    void doubleRoundtrip() throws EncodeException, DecodeException {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Double> codec = Codec.doubleCodec();
        codec.encode(buf, 2.718281828);
        buf.flip();
        assertEquals(2.718281828, codec.decode(buf), 0.0);
    }

    @Test
    void characterRoundtrip() throws EncodeException, DecodeException {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Character> codec = Codec.characterCodec();
        codec.encode(buf, 'A');
        buf.flip();
        assertEquals('A', codec.decode(buf).charValue());
    }

    @Test
    void stringUtf8Roundtrip() throws EncodeException, DecodeException {
        String[] values = {"", "hello", "你好世界2"};
        for (String val : values) {
            ByteBuffer buf = ByteBuffer.allocate(val.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + 16);
            Codec<String> codec = Codec.stringCodec();
            codec.encode(buf, val);
            buf.flip();
            assertEquals(val, codec.decode(buf), "String roundtrip for length=" + val.length());
        }
    }

    @Test
    void uuidRoundtrip() throws EncodeException, DecodeException {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buf = ByteBuffer.allocate(32);
        Codec<UUID> codec = Codec.uuidCodec();
        codec.encode(buf, uuid);
        buf.flip();
        assertEquals(uuid, codec.decode(buf));
    }

    @Test
    void byteArrayRoundtrip() throws EncodeException, DecodeException {
        byte[] arr = new byte[]{1, 2, 3, 4, 5};
        ByteBuffer buf = ByteBuffer.allocate(32);
        Codec<byte[]> codec = Codec.byteArrayCodec();
        codec.encode(buf, arr);
        buf.flip();
        assertArrayEquals(arr, codec.decode(buf));
    }

    @Test
    void fixed32Roundtrip() throws EncodeException, DecodeException {
        ByteBuffer buf = ByteBuffer.allocate(16);
        Codec<Integer> codec = Fixed32Codec.INSTANCE;
        codec.encode(buf, 42);
        codec.encode(buf, -1);
        buf.flip();
        assertEquals(42, codec.decode(buf).intValue());
        assertEquals(-1, codec.decode(buf).intValue());
    }

    @Test
    void varIntAndFixed32HaveDifferentIdentity() {
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

    @Test
    void codecIdentityIsStable() {
        assertEquals("fpt:int:varint32", Codec.integerCodec().identity());
        assertEquals("fpt:long:varlong64", Codec.longCodec().identity());
        assertEquals("fpt:boolean", Codec.booleanCodec().identity());
        assertEquals("fpt:string:utf8", Codec.stringCodec().identity());
        assertEquals("fpt:int:fixed32", Fixed32Codec.INSTANCE.identity());
    }
}