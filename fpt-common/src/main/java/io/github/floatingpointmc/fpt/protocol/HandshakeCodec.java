package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.DecodeException;
import io.github.floatingpointmc.fpt.codec.EncodeException;
import io.github.floatingpointmc.fpt.codec.VarInt;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@UtilityClass
public final class HandshakeCodec {

    public static void encode(@NotNull ByteBuffer buf, @NotNull HandshakeMessage handshake) throws EncodeException {
        byte[] idBytes = handshake.identifier().getBytes(StandardCharsets.UTF_8);
        VarInt.writeVarInt(buf, idBytes.length);
        buf.put(idBytes);
        VarInt.writeVarInt(buf, handshake.version());
        byte[] fpBytes = handshake.fingerprint().bytes();
        VarInt.writeVarInt(buf, fpBytes.length);
        buf.put(fpBytes);
    }

    public static @NotNull HandshakeMessage decode(@NotNull ByteBuffer buf) throws DecodeException {
        int idLen = VarInt.readVarInt(buf);
        byte[] idBytes = new byte[idLen];
        buf.get(idBytes);
        String identifier = new String(idBytes, StandardCharsets.UTF_8);
        int version = VarInt.readVarInt(buf);
        int fpLen = VarInt.readVarInt(buf);
        byte[] fpBytes = new byte[fpLen];
        buf.get(fpBytes);
        Fingerprint fingerprint = new Fingerprint(fpBytes);
        return new HandshakeMessage(identifier, version, fingerprint);
    }

    public static @NotNull HandshakeMessage fromProtocol(@NotNull Protocol protocol) {
        return new HandshakeMessage(protocol.identifier(), protocol.version(), protocol.fingerprint());
    }
}