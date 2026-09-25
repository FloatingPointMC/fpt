package io.github.floatingpointmc.fpt.codec.exceptions;

import org.jetbrains.annotations.NotNull;

public class CodecException extends Exception {
    public CodecException(@NotNull String message) {
        super(message);
    }

    public CodecException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}