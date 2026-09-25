package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.Codec;
import io.github.floatingpointmc.fpt.codec.Fixed32Codec;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
                .register(MessageA.class)
                .register(MessageB.class);

        Protocol b = Protocol.create()
                .register(MessageA.class)
                .register(MessageB.class);

        assertEquals(a.fingerprint(), b.fingerprint());
    }

    @Test
    void fingerprintRegistrationOrderSensitivity() {
        Protocol ab = Protocol.create()
                .register(MessageA.class)
                .register(MessageB.class);

        Protocol ba = Protocol.create()
                .register(MessageB.class)
                .register(MessageA.class);

        assertNotEquals(ab.fingerprint(), ba.fingerprint(),
                "register(A).register(B) must differ from register(B).register(A)");
    }

    @Test
    void fingerprintIdentifierSensitivity() {
        Protocol a = Protocol.create("app1", 1);
        Protocol b = Protocol.create("app2", 1);
        assertNotEquals(a.fingerprint(), b.fingerprint());
    }

    @Test
    void fingerprintVersionSensitivity() {
        Protocol a = Protocol.create("app", 1);
        Protocol b = Protocol.create("app", 2);
        assertNotEquals(a.fingerprint(), b.fingerprint());
    }

    @Test
    void fingerprintCodecSensitivity() {
        Protocol base = Protocol.create();
        Map<Class<?>, Codec<?>> customMap = new HashMap<Class<?>, Codec<?>>(base.codec());
        customMap.put(Integer.class, Fixed32Codec.INSTANCE);

        Protocol varInt = base;
        Protocol fixed32 = base.codec(customMap);

        assertNotEquals(varInt.fingerprint(), fixed32.fingerprint(),
                "Different Integer codec must produce different fingerprint");
    }

    @Test
    void fingerprintMessageDifference() {
        Protocol withA = Protocol.create().register(MessageA.class);
        Protocol withB = Protocol.create().register(MessageB.class);
        assertNotEquals(withA.fingerprint(), withB.fingerprint());
    }

    @Test
    void fingerprintS2CDifference() {
        Protocol withC = Protocol.create().registerS2C(MessageC.class);
        Protocol empty = Protocol.create();
        assertNotEquals(withC.fingerprint(), empty.fingerprint());
    }

    @Test
    void fingerprintHexIsStable() {
        Protocol p = Protocol.create().register(MessageA.class);
        String hex1 = p.fingerprint().hex();
        String hex2 = p.fingerprint().hex();
        assertEquals(hex1, hex2);
        assertTrue(hex1.length() > 0);
    }

    @Test
    void fingerprintBytesAreDefensiveCopy() {
        Protocol p = Protocol.create().register(MessageA.class);
        byte[] bytes1 = p.fingerprint().bytes();
        byte[] bytes2 = p.fingerprint().bytes();
        assertArrayEquals(bytes1, bytes2);
        bytes1[0] = (byte) ~bytes1[0];
        assertNotEquals(bytes1[0], bytes2[0]);
    }
}