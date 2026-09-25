package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.Codec;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Fingerprint {

    private final byte[] bytes;

    Fingerprint(@NotNull byte[] bytes) {
        this.bytes = bytes;
    }

    public @NotNull byte[] bytes() {
        byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
    }

    public @NotNull String hex() {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fingerprint that = (Fingerprint) o;
        if (this.bytes.length != that.bytes.length) return false;
        for (int i = 0; i < bytes.length; i++) {
            if (this.bytes[i] != that.bytes[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (byte b : bytes) {
            h = 31 * h + b;
        }
        return h;
    }

    @Override
    public @NotNull String toString() {
        return hex();
    }

    static @NotNull Fingerprint compute(@NotNull String identifier, int version,
                                         @NotNull MessageRegistry c2s, @NotNull MessageRegistry s2c,
                                         @NotNull Map<Class<?>, Codec<?>> codecMap) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(identifier.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            md.update(ByteBuffer.allocate(4).putInt(version).array());

            for (MessageRegistry.Entry entry : c2s.entries()) {
                md.update((byte) 0);
                md.update(entry.messageType().getName().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                md.update(ByteBuffer.allocate(4).putInt(entry.messageId()).array());
            }

            for (MessageRegistry.Entry entry : s2c.entries()) {
                md.update((byte) 1);
                md.update(entry.messageType().getName().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                md.update(ByteBuffer.allocate(4).putInt(entry.messageId()).array());
            }

            List<String> codecIdentities = new ArrayList<>();
            for (Map.Entry<Class<?>, Codec<?>> e : codecMap.entrySet()) {
                String key = e.getKey().getName();
                String codecId = e.getValue().identity();
                codecIdentities.add(key + "=" + codecId);
            }
            Collections.sort(codecIdentities);
            for (String s : codecIdentities) {
                md.update((byte) 2);
                md.update(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            return new Fingerprint(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}