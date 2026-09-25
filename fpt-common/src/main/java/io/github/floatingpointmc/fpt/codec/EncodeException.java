package io.github.floatingpointmc.fpt.codec;

import org.jetbrains.annotations.NotNull;

public class EncodeException extends CodecException {

    public EncodeException(@NotNull String message) {
        super(message);
    }

    public EncodeException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}