package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.CodecMap;
import io.github.floatingpointmc.fpt.codec.Fixed32Codec;
import io.github.floatingpointmc.fpt.codec.MessageCodec;
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.message.impl.S2CMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
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
    void defaultProtocolHasDefaultIdentifierAndVersion() {
        Protocol p = Protocol.create();
        assertEquals("fpt", p.getIdentifier());
        assertEquals(1, p.getVersion());
    }

    @Test
    void createWithCustomIdentifierAndVersion() {
        Protocol p = Protocol.create("myapp", 3);
        assertEquals("myapp", p.getIdentifier());
        assertEquals(3, p.getVersion());
    }

    @Test
    void registerC2SReturnsNewProtocol() {
        Protocol base = Protocol.create();
        Protocol a = base.registerC2S(MessageA.class);
        Protocol b = base.registerC2S(MessageB.class);

        assertEquals(0, base.getC2sRegistry().size());
        assertEquals(1, a.getC2sRegistry().size());
        assertEquals(1, b.getC2sRegistry().size());
        assertTrue(a.getC2sRegistry().isRegistered(MessageA.class));
        assertFalse(a.getC2sRegistry().isRegistered(MessageB.class));
        assertTrue(b.getC2sRegistry().isRegistered(MessageB.class));
        assertFalse(b.getC2sRegistry().isRegistered(MessageA.class));
    }

    @Test
    void registerS2CReturnsNewProtocol() {
        Protocol base = Protocol.create();
        Protocol withS2C = base.registerS2C(MessageC.class);

        assertEquals(0, base.getS2cRegistry().size());
        assertEquals(1, withS2C.getS2cRegistry().size());
        assertTrue(withS2C.getS2cRegistry().isRegistered(MessageC.class));
    }

    @Test
    void chainedRegistrationIsImmutable() {
        Protocol base = Protocol.create();
        Protocol a = base.registerC2S(MessageA.class);
        Protocol ab = a.registerC2S(MessageB.class);

        assertEquals(0, base.getC2sRegistry().size());
        assertEquals(1, a.getC2sRegistry().size());
        assertEquals(2, ab.getC2sRegistry().size());
        assertTrue(ab.getC2sRegistry().isRegistered(MessageA.class));
        assertTrue(ab.getC2sRegistry().isRegistered(MessageB.class));
        assertFalse(a.getC2sRegistry().isRegistered(MessageB.class));
    }

    @Test
    void messageIdAssignment() {
        Protocol protocol = Protocol.create()
                .registerC2S(MessageA.class)
                .registerC2S(MessageB.class);

        assertEquals(0, protocol.getC2sRegistry().getMessageId(MessageA.class));
        assertEquals(1, protocol.getC2sRegistry().getMessageId(MessageB.class));
    }

    @Test
    void duplicateRegistrationThrows() {
        Protocol a = Protocol.create().registerC2S(MessageA.class);
        assertThrows(IllegalArgumentException.class, () -> a.registerC2S(MessageA.class));
    }

    @Test
    void codecReturnsSnapshot() {
        Protocol base = Protocol.create();
        CodecMap exposed = base.codec();
        assertNotNull(exposed.get(Integer.class));

        CodecMap modified = new CodecMap(exposed);
        modified.put(Integer.class, Fixed32Codec.INSTANCE);
        Protocol custom = base.codec(modified);

        assertEquals("fpt:int:varint32", base.codec().get(Integer.class).identity());
        assertEquals("fpt:int:fixed32", custom.codec().get(Integer.class).identity());
    }

    @Test
    void codecOverrideDoesNotAffectDefault() {
        CodecMap map = new CodecMap(Protocol.create().codec());
        map.put(Integer.class, Fixed32Codec.INSTANCE);
        Protocol custom = Protocol.create().codec(map);

        assertEquals("fpt:int:varint32", Protocol.create().codec().get(Integer.class).identity());
        assertEquals("fpt:int:fixed32", custom.codec().get(Integer.class).identity());
    }

    @Test
    void getMessageCodecReturnsCorrectCodec() {
        Protocol protocol = Protocol.create().registerC2S(MessageA.class);
        MessageCodec<MessageA> codec = protocol.getMessageCodec(MessageA.class);
        assertNotNull(codec);
    }

    @Test
    void getMessageIdReturnsCorrectId() {
        Protocol protocol = Protocol.create()
                .registerC2S(MessageA.class)
                .registerS2C(MessageC.class);
        assertEquals(0, protocol.getMessageId(MessageA.class));
        assertEquals(0, protocol.getMessageId(MessageC.class));
    }
}