package io.github.floatingpointmc.fpt.protocol;

import org.jetbrains.annotations.NotNull;

public final class HandshakeMessage {

    private final @NotNull String identifier;
    private final int version;
    private final @NotNull Fingerprint fingerprint;

    public HandshakeMessage(@NotNull String identifier, int version, @NotNull Fingerprint fingerprint) {
        this.identifier = identifier;
        this.version = version;
        this.fingerprint = fingerprint;
    }

    public @NotNull String identifier() {
        return identifier;
    }

    public int version() {
        return version;
    }

    public @NotNull Fingerprint fingerprint() {
        return fingerprint;
    }

    public boolean matches(@NotNull Protocol protocol) {
        return this.identifier.equals(protocol.identifier())
                && this.version == protocol.version()
                && this.fingerprint.equals(protocol.fingerprint());
    }
}