package io.github.floatingpointmc.fpt.codec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodecMapTest {

    @Test
    void createReturnsEmptyMap() {
        CodecMap map = CodecMap.create();
        assertEquals(0, map.size());
    }

    @Test
    void putAndGet() {
        CodecMap map = CodecMap.create();
        Codec<Integer> codec = Codec.integerCodec();
        map.put(Integer.class, codec);
        assertSame(codec, map.get(Integer.class));
        assertEquals(1, map.size());
    }

    @Test
    void containsKey() {
        CodecMap map = CodecMap.create();
        assertFalse(map.containsKey(Integer.class));
        map.put(Integer.class, Codec.integerCodec());
        assertTrue(map.containsKey(Integer.class));
    }

    @Test
    void getMissingKeyReturnsNull() {
        CodecMap map = CodecMap.create();
        assertNull(map.get(Integer.class));
    }

    @Test
    void copyConstructorIsIndependent() {
        CodecMap original = CodecMap.create();
        original.put(Integer.class, Codec.integerCodec());

        CodecMap copy = new CodecMap(original);
        assertNotSame(original, copy);
        assertSame(original.get(Integer.class), copy.get(Integer.class));

        copy.put(String.class, Codec.stringCodec());
        assertFalse(original.containsKey(String.class));
        assertTrue(copy.containsKey(String.class));
    }

    @Test
    void copyPreservesAllEntries() {
        CodecMap original = CodecMap.create();
        original.put(Integer.class, Codec.integerCodec());
        original.put(String.class, Codec.stringCodec());

        CodecMap copy = new CodecMap(original);
        assertEquals(original.size(), copy.size());
        assertSame(original.get(Integer.class), copy.get(Integer.class));
        assertSame(original.get(String.class), copy.get(String.class));
    }

    @Test
    void putReturnsSelfForChaining() {
        CodecMap map = CodecMap.create();
        CodecMap result = map.put(Integer.class, Codec.integerCodec());
        assertSame(map, result);
    }

    @Test
    void putOverwrites() {
        CodecMap map = CodecMap.create();
        map.put(Integer.class, Codec.integerCodec());
        map.put(Integer.class, Fixed32Codec.INSTANCE);
        assertSame(Fixed32Codec.INSTANCE, map.get(Integer.class));
        assertEquals(1, map.size());
    }

    @Test
    void defaultCodecMapIsIndependentCopy() {
        CodecMap a = Codec.defaultCodecMap();
        CodecMap b = Codec.defaultCodecMap();
        assertNotSame(a, b);
        a.put(Integer.class, Fixed32Codec.INSTANCE);
        assertSame(Codec.integerCodec(), b.get(Integer.class));
    }
}