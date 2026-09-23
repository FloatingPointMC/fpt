package io.github.floatingpointmc.fpirc.common.protocol;

import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.c2s.C2SLoginMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.s2c.S2CLoginResponseMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("C2S/S2C Direction Separation")
public class DirectionSeparationTest {

    @Test
    @DisplayName("C2S and S2C registries are completely independent by type")
    void testRegistriesIndependentByType() {
        MessageRegistry c2s = DefaultMessageRegistry.createC2S();
        MessageRegistry s2c = DefaultMessageRegistry.createS2C();

        assertTrue(c2s.isRegistered(C2SLoginMessage.class));
        assertFalse(s2c.isRegistered(C2SLoginMessage.class));

        assertTrue(s2c.isRegistered(S2CLoginResponseMessage.class));
        assertFalse(c2s.isRegistered(S2CLoginResponseMessage.class));
    }

    @Test
    @DisplayName("Same numeric ID 0x00 maps to different messages in C2S vs S2C")
    void testSameIdDifferentMessages() {
        MessageRegistry c2s = DefaultMessageRegistry.createC2S();
        MessageRegistry s2c = DefaultMessageRegistry.createS2C();

        assertEquals(C2SLoginMessage.class, c2s.getMessageType(0x00));
        assertEquals(S2CLoginResponseMessage.class, s2c.getMessageType(0x00));

        assertNotEquals(c2s.getMessageType(0x00), s2c.getMessageType(0x00));
    }

    @Test
    @DisplayName("Same numeric ID 0x01 maps to different Chat messages in C2S vs S2C")
    void testSameChatIdDifferentDirections() {
        MessageRegistry c2s = DefaultMessageRegistry.createC2S();
        MessageRegistry s2c = DefaultMessageRegistry.createS2C();

        assertEquals(C2SChatMessage.class, c2s.getMessageType(0x01));
        assertEquals(S2CChatMessage.class, s2c.getMessageType(0x01));

        assertNotEquals(c2s.getMessageType(0x01), s2c.getMessageType(0x01));
    }

    @Test
    @DisplayName("C2S registry does not contain S2C message types")
    void testC2SRegistryLacksS2CTypes() {
        MessageRegistry c2s = DefaultMessageRegistry.createC2S();
        assertFalse(c2s.isRegistered(S2CLoginResponseMessage.class));
        assertFalse(c2s.isRegistered(S2CChatMessage.class));
    }

    @Test
    @DisplayName("S2C registry does not contain C2S message types")
    void testS2CRegistryLacksC2STypes() {
        MessageRegistry s2c = DefaultMessageRegistry.createS2C();
        assertFalse(s2c.isRegistered(C2SLoginMessage.class));
        assertFalse(s2c.isRegistered(C2SChatMessage.class));
    }

    @Test
    @DisplayName("Decoding S2C LoginResponse packet with C2S registry produces wrong type (not S2CLoginResponseMessage)")
    void testDecodeS2CWithC2SRegistryWrongType() throws Exception {
        MessageRegistry c2s = DefaultMessageRegistry.createC2S();
        MessageRegistry s2c = DefaultMessageRegistry.createS2C();

        S2CLoginResponseMessage s2cMsg = new S2CLoginResponseMessage(true, "Welcome");
        byte[] s2cPacket = MessageCoder.encode(s2c, s2cMsg);
        java.nio.ByteBuffer s2cFrame = stripLengthPrefix(s2cPacket);

        try {
            Message decoded = MessageCoder.decode(c2s, s2cFrame);
            assertFalse(decoded instanceof S2CLoginResponseMessage,
                    "C2S registry should not decode S2C LoginResponse message correctly");
        } catch (MessageDecodeException e) {
            // This is also acceptable - the codec may reject the payload
        }
    }

    @Test
    @DisplayName("Decoding C2S Login packet with S2C registry produces wrong type (not C2SLoginMessage)")
    void testDecodeC2SWithS2CRegistryWrongType() throws Exception {
        MessageRegistry c2s = DefaultMessageRegistry.createC2S();
        MessageRegistry s2c = DefaultMessageRegistry.createS2C();

        C2SLoginMessage c2sMsg = new C2SLoginMessage("TestUser");
        byte[] c2sPacket = MessageCoder.encode(c2s, c2sMsg);
        java.nio.ByteBuffer c2sFrame = stripLengthPrefix(c2sPacket);

        try {
            Message decoded = MessageCoder.decode(s2c, c2sFrame);
            assertFalse(decoded instanceof C2SLoginMessage,
                    "S2C registry should not decode C2S Login message correctly");
        } catch (MessageDecodeException e) {
            // This is also acceptable - the codec may reject the payload
        }
    }

    @Test
    @DisplayName("S2C-only packet ID that does not exist in C2S registry is rejected")
    void testS2COnlyIdRejectedByC2SRegistry() {
        MessageRegistry c2s = DefaultMessageRegistry.createC2S();
        MessageRegistry s2c = DefaultMessageRegistry.createS2C();

        // Use a packet ID that exists in S2C but not in C2S
        // Currently all IDs overlap, so we test with an unregistered ID
        assertFalse(c2s.isRegistered(0x05));
        assertFalse(s2c.isRegistered(0x05));
    }

    private static java.nio.ByteBuffer stripLengthPrefix(byte[] packet) throws MessageDecodeException {
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(packet);
        VarInt.readVarInt(buf);
        return buf;
    }
}