package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.Codec;
import io.github.floatingpointmc.fpt.codec.Fixed32Codec;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {

    public static class MessageA implements C2SMessage {
        public int value;
    }

    public static class MessageB implements C2SMessage {
        public String text;
    }

    public static class MessageC implements S2CMessage {
        public int code;
    }

    @Test
    void protocolIsImmutable() {
        Protocol base = Protocol.create();
        Protocol a = base.register(MessageA.class);
        Protocol b = base.register(MessageB.class);

        assertEquals(0, base.c2sRegistry().size());
        assertEquals(1, a.c2sRegistry().size());
        assertEquals(1, b.c2sRegistry().size());

        assertTrue(a.c2sRegistry().isRegistered(MessageA.class));
        assertFalse(a.c2sRegistry().isRegistered(MessageB.class));

        assertTrue(b.c2sRegistry().isRegistered(MessageB.class));
        assertFalse(b.c2sRegistry().isRegistered(MessageA.class));
    }

    @Test
    void registryIsImmutable() {
        Protocol base = Protocol.create();
        Protocol a = base.register(MessageA.class);
        Protocol ab = a.register(MessageB.class);

        assertEquals(0, base.c2sRegistry().size());
        assertEquals(1, a.c2sRegistry().size());
        assertEquals(2, ab.c2sRegistry().size());

        assertTrue(ab.c2sRegistry().isRegistered(MessageA.class));
        assertTrue(ab.c2sRegistry().isRegistered(MessageB.class));
        assertFalse(a.c2sRegistry().isRegistered(MessageB.class));
    }

    @Test
    void codecMapIsImmutable() {
        Protocol protocol = Protocol.create();
        Map<Class<?>, Codec<?>> codecMap = protocol.codec();
        assertThrows(UnsupportedOperationException.class, () -> {
            codecMap.put(Integer.class, Fixed32Codec.INSTANCE);
        });
    }

    @Test
    void codecMapSnapshot() {
        Protocol base = Protocol.create();
        Map<Class<?>, Codec<?>> map = new HashMap<Class<?>, Codec<?>>(base.codec());
        map.put(Integer.class, Fixed32Codec.INSTANCE);

        Protocol custom = base.codec(map);
        map.clear();

        assertSame(Fixed32Codec.INSTANCE, custom.codec().get(Integer.class));
    }

    @Test
    void defaultProtocolIsolation() {
        Protocol custom = Protocol.DEFAULT_PROTOCOL.register(MessageA.class);

        assertEquals(0, Protocol.DEFAULT_PROTOCOL.c2sRegistry().size());
        assertEquals(1, custom.c2sRegistry().size());
    }

    @Test
    void registerS2C() {
        Protocol base = Protocol.create();
        Protocol withS2C = base.registerS2C(MessageC.class);

        assertEquals(0, base.s2cRegistry().size());
        assertEquals(1, withS2C.s2cRegistry().size());
        assertTrue(withS2C.s2cRegistry().isRegistered(MessageC.class));
    }

    @Test
    void messageIdAssignment() {
        Protocol protocol = Protocol.create()
                .register(MessageA.class)
                .register(MessageB.class);

        assertEquals(0, protocol.c2sRegistry().getMessageId(MessageA.class));
        assertEquals(1, protocol.c2sRegistry().getMessageId(MessageB.class));
    }

    @Test
    void codecOverrideDoesNotAffectDefault() {
        Map<Class<?>, Codec<?>> map = new HashMap<Class<?>, Codec<?>>(Protocol.DEFAULT_PROTOCOL.codec());
        map.put(Integer.class, Fixed32Codec.INSTANCE);
        Protocol custom = Protocol.DEFAULT_PROTOCOL.codec(map);

        assertEquals("fpt:int:varint32", Protocol.DEFAULT_PROTOCOL.codec().get(Integer.class).identity());
        assertEquals("fpt:int:fixed32", custom.codec().get(Integer.class).identity());
    }

    @Test
    void protocolCreateWithIdentifierAndVersion() {
        Protocol p = Protocol.create("myapp", 3);
        assertEquals("myapp", p.identifier());
        assertEquals(3, p.version());
    }

    @Test
    void defaultProtocolHasDefaultIdentifierAndVersion() {
        assertEquals("fpt", Protocol.DEFAULT_PROTOCOL.identifier());
        assertEquals(1, Protocol.DEFAULT_PROTOCOL.version());
    }
}