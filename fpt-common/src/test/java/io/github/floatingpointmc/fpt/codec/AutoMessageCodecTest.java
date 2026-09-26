package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
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
    void autoCodecRoundtrip() throws Exception {
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
    void autoCodecWithDifferentCodecMaps() throws Exception {
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

        assertNotEquals(varIntSize, fixed32Size);

        SimpleMessage decodedVarInt = varIntCodec.decode(varIntBuf);
        SimpleMessage decodedFixed32 = fixed32Codec.decode(fixed32Buf);
        assertEquals(42, decodedVarInt.value);
        assertEquals(42, decodedFixed32.value);
    }

    @Test
    void unsupportedFieldThrowsOnRegister() {
        assertThrows(IllegalArgumentException.class, () -> Protocol.create().registerC2S(BadMessage.class));
    }
}