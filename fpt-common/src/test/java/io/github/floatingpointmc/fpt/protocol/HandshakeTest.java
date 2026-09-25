package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.DecodeException;
import io.github.floatingpointmc.fpt.codec.EncodeException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class HandshakeTest {

    @Test
    void handshakeRoundtrip() throws Exception {
        Protocol protocol = Protocol.create("test-app", 2);
        HandshakeMessage original = HandshakeCodec.fromProtocol(protocol);

        ByteBuffer buf = ByteBuffer.allocate(256);
        HandshakeCodec.encode(buf, original);
        buf.flip();

        HandshakeMessage decoded = HandshakeCodec.decode(buf);
        assertEquals(original.identifier(), decoded.identifier());
        assertEquals(original.version(), decoded.version());
        assertEquals(original.fingerprint(), decoded.fingerprint());
    }

    @Test
    void handshakeMatch() {
        Protocol protocol = Protocol.create("test-app", 2);
        HandshakeMessage handshake = HandshakeCodec.fromProtocol(protocol);
        assertTrue(handshake.matches(protocol));
    }

    @Test
    void handshakeIdentifierMismatch() {
        Protocol server = Protocol.create("server-app", 1);
        Protocol client = Protocol.create("client-app", 1);
        HandshakeMessage clientHandshake = HandshakeCodec.fromProtocol(client);
        assertFalse(clientHandshake.matches(server));
    }

    @Test
    void handshakeVersionMismatch() {
        Protocol server = Protocol.create("app", 1);
        Protocol client = Protocol.create("app", 2);
        HandshakeMessage clientHandshake = HandshakeCodec.fromProtocol(client);
        assertFalse(clientHandshake.matches(server));
    }

    @Test
    void handshakeFingerprintMismatch() {
        Protocol server = Protocol.create().register(ProtocolTest.MessageA.class);
        Protocol client = Protocol.create().register(ProtocolTest.MessageB.class);
        HandshakeMessage clientHandshake = HandshakeCodec.fromProtocol(client);
        assertFalse(clientHandshake.matches(server));
    }
}