package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.CodecMap;
import io.github.floatingpointmc.fpt.codec.Fixed32Codec;
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.message.impl.S2CMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class FingerprintTest {

    static class MessageA implements C2SMessage {
        int value;
    }

    static class MessageB implements C2SMessage {
        String text;
    }

    static class MessageC implements S2CMessage {
        int code;
    }

    @Test
    void fingerprintDeterminism() {
        Protocol a = Protocol.create()
                .registerC2S(MessageA.class)
                .registerC2S(MessageB.class);

        Protocol b = Protocol.create()
                .registerC2S(MessageA.class)
                .registerC2S(MessageB.class);

        assertEquals(a.getFingerprint(), b.getFingerprint());
    }

    @Test
    void fingerprintRegistrationOrderSensitivity() {
        Protocol ab = Protocol.create()
                .registerC2S(MessageA.class)
                .registerC2S(MessageB.class);

        Protocol ba = Protocol.create()
                .registerC2S(MessageB.class)
                .registerC2S(MessageA.class);

        assertNotEquals(ab.getFingerprint(), ba.getFingerprint(),
                "register(A).register(B) must differ from register(B).register(A)");
    }

    @Test
    void fingerprintIdentifierSensitivity() {
        Protocol a = Protocol.create("app1", 1);
        Protocol b = Protocol.create("app2", 1);
        assertNotEquals(a.getFingerprint(), b.getFingerprint());
    }

    @Test
    void fingerprintVersionSensitivity() {
        Protocol a = Protocol.create("app", 1);
        Protocol b = Protocol.create("app", 2);
        assertNotEquals(a.getFingerprint(), b.getFingerprint());
    }

    @Test
    void fingerprintCodecSensitivity() {
        Protocol base = Protocol.create();
        CodecMap customMap = new CodecMap(base.codec());
        customMap.put(Integer.class, Fixed32Codec.INSTANCE);

        Protocol fixed32 = base.codec(customMap);

        assertNotEquals(base.getFingerprint(), fixed32.getFingerprint(),
                "Different Integer codec must produce different fingerprint");
    }

    @Test
    void fingerprintMessageDifference() {
        Protocol withA = Protocol.create().registerC2S(MessageA.class);
        Protocol withB = Protocol.create().registerC2S(MessageB.class);
        assertNotEquals(withA.getFingerprint(), withB.getFingerprint());
    }

    @Test
    void fingerprintS2CDifference() {
        Protocol withC = Protocol.create().registerS2C(MessageC.class);
        Protocol empty = Protocol.create();
        assertNotEquals(withC.getFingerprint(), empty.getFingerprint());
    }

    @Test
    void fingerprintHexIsStable() {
        Protocol p = Protocol.create().registerC2S(MessageA.class);
        String hex1 = p.getFingerprint().hex();
        String hex2 = p.getFingerprint().hex();
        assertEquals(hex1, hex2);
        assertFalse(hex1.isEmpty());
    }

    @Test
    void fingerprintBytesAreDefensiveCopy() {
        Protocol p = Protocol.create().registerC2S(MessageA.class);
        byte[] bytes1 = p.getFingerprint().bytes();
        byte[] bytes2 = p.getFingerprint().bytes();
        assertArrayEquals(bytes1, bytes2);
        bytes1[0] = (byte) ~bytes1[0];
        assertNotEquals(bytes1[0], bytes2[0]);
    }
}