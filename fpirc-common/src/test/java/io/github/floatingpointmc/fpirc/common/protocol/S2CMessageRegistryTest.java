package io.github.floatingpointmc.fpirc.common.protocol;

import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CDisconnectMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CLoginResponseMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CMessageIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("S2C MessageRegistry")
public class S2CMessageRegistryTest {

    private MessageRegistry registry;

    @BeforeEach
    void setUp() {
        registry = DefaultMessageRegistry.createS2C();
    }

    @Test
    @DisplayName("S2C registry contains LoginResponse message at 0x00")
    void testLoginResponseRegistered() {
        assertTrue(registry.isRegistered(S2CMessageIds.LOGIN_RESPONSE));
        assertEquals(S2CLoginResponseMessage.class, registry.getMessageType(S2CMessageIds.LOGIN_RESPONSE));
        assertEquals(S2CMessageIds.LOGIN_RESPONSE, registry.getPacketId(S2CLoginResponseMessage.class));
    }

    @Test
    @DisplayName("S2C registry contains Chat message at 0x01")
    void testChatRegistered() {
        assertTrue(registry.isRegistered(S2CMessageIds.CHAT));
        assertEquals(S2CChatMessage.class, registry.getMessageType(S2CMessageIds.CHAT));
        assertEquals(S2CMessageIds.CHAT, registry.getPacketId(S2CChatMessage.class));
    }

    @Test
    @DisplayName("S2C registry contains Disconnect message at 0x02")
    void testDisconnectRegistered() {
        assertTrue(registry.isRegistered(S2CMessageIds.DISCONNECT));
        assertEquals(S2CDisconnectMessage.class, registry.getMessageType(S2CMessageIds.DISCONNECT));
        assertEquals(S2CMessageIds.DISCONNECT, registry.getPacketId(S2CDisconnectMessage.class));
    }

    @Test
    @DisplayName("S2C codec can encode and decode LoginResponse message")
    void testLoginResponseCodecRoundTrip() throws Exception {
        S2CLoginResponseMessage original = new S2CLoginResponseMessage(true, "Welcome");
        byte[] packet = MessageCoder.encode(registry, original);
        java.nio.ByteBuffer frame = stripLengthPrefix(packet);
        Message decoded = MessageCoder.decode(registry, frame);
        S2CLoginResponseMessage loginResp = (S2CLoginResponseMessage) decoded;
        assertTrue(loginResp.isSuccess());
        assertEquals("Welcome", loginResp.getReason());
    }

    @Test
    @DisplayName("S2C codec can encode and decode Chat message")
    void testChatCodecRoundTrip() throws Exception {
        S2CChatMessage original = new S2CChatMessage("#general", "User: Hello!");
        byte[] packet = MessageCoder.encode(registry, original);
        java.nio.ByteBuffer frame = stripLengthPrefix(packet);
        Message decoded = MessageCoder.decode(registry, frame);
        S2CChatMessage chat = (S2CChatMessage) decoded;
        assertEquals("#general", chat.getChannel());
        assertEquals("User: Hello!", chat.getContent());
    }

    @Test
    @DisplayName("S2C codec can encode and decode Disconnect message")
    void testDisconnectCodecRoundTrip() throws Exception {
        S2CDisconnectMessage original = new S2CDisconnectMessage("Server shutting down");
        byte[] packet = MessageCoder.encode(registry, original);
        java.nio.ByteBuffer frame = stripLengthPrefix(packet);
        Message decoded = MessageCoder.decode(registry, frame);
        assertEquals("Server shutting down", ((S2CDisconnectMessage) decoded).getReason());
    }

    @Test
    @DisplayName("S2C registry rejects unknown packet IDs")
    void testRejectsUnknownPacketId() {
        assertFalse(registry.isRegistered(0x05));
        assertFalse(registry.isRegistered(0xFF));
    }

    @Test
    @DisplayName("S2C registry does not contain C2S message types")
    void testDoesNotContainC2STypes() {
        assertFalse(registry.isRegistered(io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SLoginMessage.class));
        assertFalse(registry.isRegistered(io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SChatMessage.class));
        assertFalse(registry.isRegistered(io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SDisconnectMessage.class));
    }

    private static java.nio.ByteBuffer stripLengthPrefix(byte[] packet) throws MessageDecodeException {
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(packet);
        VarInt.readVarInt(buf);
        return buf;
    }
}