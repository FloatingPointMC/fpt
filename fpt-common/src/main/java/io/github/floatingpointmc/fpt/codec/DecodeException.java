package io.github.floatingpointmc.fpt.codec;

import org.jetbrains.annotations.NotNull;

public class DecodeException extends CodecException {

    public DecodeException(@NotNull String message) {
        super(message);
    }

    public DecodeException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}