package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.protocol.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.Message;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.S2CMessage;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AutoMessageCodecTest {

    public static class TestMessage implements C2SMessage {
        public String text;
        public int id;
        public UUID userId;
    }

    public static class SimpleMessage implements C2SMessage {
        public int value;
    }

    public static class BadMessage implements C2SMessage {
        public Thread unsupportedField;
    }

    @Test
    void automaticMessageCodec() throws Exception {
        Protocol protocol = Protocol.create().register(TestMessage.class);
        MessageCodec<TestMessage> codec = protocol.getMessageCodec(TestMessage.class);

        TestMessage msg = new TestMessage();
        msg.text = "hello";
        msg.id = 42;
        msg.userId = UUID.randomUUID();

        ByteBuffer buf = ByteBuffer.allocate(256);
        codec.encode(buf, msg);
        buf.flip();

        TestMessage decoded = codec.decode(buf);
        assertEquals(msg.text, decoded.text);
        assertEquals(msg.id, decoded.id);
        assertEquals(msg.userId, decoded.userId);
    }

    @Test
    void automaticCodecUsesCurrentProtocolCodecMap() throws Exception {
        Protocol varIntProtocol = Protocol.create().register(SimpleMessage.class);

        Map<Class<?>, Codec<?>> fixedMap = new HashMap<Class<?>, Codec<?>>(Protocol.DEFAULT_PROTOCOL.codec());
        fixedMap.put(Integer.class, Fixed32Codec.INSTANCE);
        Protocol fixed32Protocol = Protocol.DEFAULT_PROTOCOL.codec(fixedMap).register(SimpleMessage.class);

        SimpleMessage msg = new SimpleMessage();
        msg.value = 42;

        ByteBuffer varIntBuf = ByteBuffer.allocate(256);
        MessageCodec<SimpleMessage> varIntCodec = varIntProtocol.getMessageCodec(SimpleMessage.class);
        varIntCodec.encode(varIntBuf, msg);
        varIntBuf.flip();
        int varIntSize = varIntBuf.remaining();

        ByteBuffer fixed32Buf = ByteBuffer.allocate(256);
        MessageCodec<SimpleMessage> fixed32Codec = fixed32Protocol.getMessageCodec(SimpleMessage.class);
        fixed32Codec.encode(fixed32Buf, msg);
        fixed32Buf.flip();
        int fixed32Size = fixed32Buf.remaining();

        assertNotEquals(varIntSize, fixed32Size,
                "VarInt and Fixed32 encoding must produce different sizes for value 42");

        SimpleMessage decodedVarInt = varIntCodec.decode(varIntBuf);
        SimpleMessage decodedFixed32 = fixed32Codec.decode(fixed32Buf);
        assertEquals(42, decodedVarInt.value);
        assertEquals(42, decodedFixed32.value);
    }

    @Test
    void unsupportedFieldEarlyFailure() {
        assertThrows(IllegalArgumentException.class, () -> {
            Protocol.create().register(BadMessage.class);
        });
    }

    @Test
    void codecOverrideDoesNotAffectDefaultProtocol() throws Exception {
        Protocol varIntDefault = Protocol.create().register(SimpleMessage.class);

        Map<Class<?>, Codec<?>> fixedMap = new HashMap<Class<?>, Codec<?>>(Protocol.DEFAULT_PROTOCOL.codec());
        fixedMap.put(Integer.class, Fixed32Codec.INSTANCE);
        Protocol customBase = Protocol.DEFAULT_PROTOCOL.codec(fixedMap);
        Protocol fixed32Custom = customBase.register(SimpleMessage.class);

        SimpleMessage msg = new SimpleMessage();
        msg.value = 100;

        ByteBuffer defaultBuf = ByteBuffer.allocate(256);
        MessageCodec<SimpleMessage> defaultCodec = varIntDefault.getMessageCodec(SimpleMessage.class);
        defaultCodec.encode(defaultBuf, msg);
        defaultBuf.flip();

        SimpleMessage decodedDefault = defaultCodec.decode(defaultBuf);
        assertEquals(100, decodedDefault.value);

        ByteBuffer customBuf = ByteBuffer.allocate(256);
        MessageCodec<SimpleMessage> customCodec = fixed32Custom.getMessageCodec(SimpleMessage.class);
        customCodec.encode(customBuf, msg);
        customBuf.flip();

        SimpleMessage decodedCustom = customCodec.decode(customBuf);
        assertEquals(100, decodedCustom.value);

        assertEquals("fpt:int:varint32", varIntDefault.codec().get(Integer.class).identity());
        assertEquals("fpt:int:fixed32", fixed32Custom.codec().get(Integer.class).identity());
    }
}