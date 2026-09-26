package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
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
        Protocol protocol = Protocol.create().registerC2S(TestMessage.class);
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
        Protocol varIntProtocol = Protocol.create().registerC2S(SimpleMessage.class);

        CodecMap fixedMap = new CodecMap(Protocol.create().codec());
        fixedMap.put(Integer.class, Fixed32Codec.INSTANCE);
        Protocol fixed32Protocol = Protocol.create().codec(fixedMap).registerC2S(SimpleMessage.class);

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
        assertThrows(IllegalArgumentException.class, () -> Protocol.create().registerC2S(BadMessage.class));
    }

    @Test
    void codecOverrideDoesNotAffectDefaultProtocol() throws Exception {
        Protocol varIntDefault = Protocol.create().registerC2S(SimpleMessage.class);

        CodecMap fixedMap = Codec.defaultCodecMap();
        fixedMap.put(Integer.class, Fixed32Codec.INSTANCE);
        Protocol customBase = Protocol.create().codec(fixedMap);
        Protocol fixed32Custom = customBase.registerC2S(SimpleMessage.class);

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

        assertEquals("fpt:int:varint32", Objects.requireNonNull(varIntDefault.codec().get(Integer.class)).identity());
        assertEquals("fpt:int:fixed32", Objects.requireNonNull(fixed32Custom.codec().get(Integer.class)).identity());
    }
}