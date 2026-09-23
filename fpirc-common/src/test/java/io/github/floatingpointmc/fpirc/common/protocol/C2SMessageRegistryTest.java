package io.github.floatingpointmc.fpirc.common.protocol;

import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SDisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SLoginMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SMessageIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("C2S MessageRegistry")
public class C2SMessageRegistryTest {

    private MessageRegistry registry;

    @BeforeEach
    void setUp() {
        registry = DefaultMessageRegistry.createC2S();
    }

    @Test
    @DisplayName("C2S registry contains Login message at 0x00")
    void testLoginRegistered() {
        assertTrue(registry.isRegistered(C2SMessageIds.LOGIN));
        assertEquals(C2SLoginMessage.class, registry.getMessageType(C2SMessageIds.LOGIN));
        assertEquals(C2SMessageIds.LOGIN, registry.getPacketId(C2SLoginMessage.class));
    }

    @Test
    @DisplayName("C2S registry contains Chat message at 0x01")
    void testChatRegistered() {
        assertTrue(registry.isRegistered(C2SMessageIds.CHAT));
        assertEquals(C2SChatMessage.class, registry.getMessageType(C2SMessageIds.CHAT));
        assertEquals(C2SMessageIds.CHAT, registry.getPacketId(C2SChatMessage.class));
    }

    @Test
    @DisplayName("C2S registry contains Disconnect message at 0x02")
    void testDisconnectRegistered() {
        assertTrue(registry.isRegistered(C2SMessageIds.DISCONNECT));
        assertEquals(C2SDisconnectMessage.class, registry.getMessageType(C2SMessageIds.DISCONNECT));
        assertEquals(C2SMessageIds.DISCONNECT, registry.getPacketId(C2SDisconnectMessage.class));
    }

    @Test
    @DisplayName("C2S codec can encode and decode Login message")
    void testLoginCodecRoundTrip() throws Exception {
        C2SLoginMessage original = new C2SLoginMessage("TestUser");
        byte[] packet = MessageCoder.encode(registry, original);
        java.nio.ByteBuffer frame = stripLengthPrefix(packet);
        Message decoded = MessageCoder.decode(registry, frame);
        assertEquals("TestUser", ((C2SLoginMessage) decoded).getUsername());
    }

    @Test
    @DisplayName("C2S codec can encode and decode Chat message")
    void testChatCodecRoundTrip() throws Exception {
        C2SChatMessage original = new C2SChatMessage("#general", "Hello!");
        byte[] packet = MessageCoder.encode(registry, original);
        java.nio.ByteBuffer frame = stripLengthPrefix(packet);
        Message decoded = MessageCoder.decode(registry, frame);
        C2SChatMessage chat = (C2SChatMessage) decoded;
        assertEquals("#general", chat.getChannel());
        assertEquals("Hello!", chat.getContent());
    }

    @Test
    @DisplayName("C2S codec can encode and decode Disconnect message")
    void testDisconnectCodecRoundTrip() throws Exception {
        C2SDisconnectMessage original = new C2SDisconnectMessage("Goodbye");
        byte[] packet = MessageCoder.encode(registry, original);
        java.nio.ByteBuffer frame = stripLengthPrefix(packet);
        Message decoded = MessageCoder.decode(registry, frame);
        assertEquals("Goodbye", ((C2SDisconnectMessage) decoded).getReason());
    }

    @Test
    @DisplayName("C2S registry rejects unknown packet IDs")
    void testRejectsUnknownPacketIds() {
        assertFalse(registry.isRegistered(0x05));
        assertFalse(registry.isRegistered(0xFF));
    }

    @Test
    @DisplayName("C2S registry does not contain S2C message types")
    void testDoesNotContainS2CTypes() {
        assertFalse(registry.isRegistered(io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CLoginResponseMessage.class));
        assertFalse(registry.isRegistered(io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage.class));
        assertFalse(registry.isRegistered(io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CDisconnectMessage.class));
    }

    private static java.nio.ByteBuffer stripLengthPrefix(byte[] packet) throws MessageDecodeException {
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(packet);
        VarInt.readVarInt(buf);
        return buf;
    }
}