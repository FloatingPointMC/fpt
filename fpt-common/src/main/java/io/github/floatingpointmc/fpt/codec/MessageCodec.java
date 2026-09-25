package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.protocol.Message;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public interface MessageCodec<T extends Message> {

    void encode(@NotNull ByteBuffer buf, @NotNull T message) throws EncodeException;

    @NotNull T decode(@NotNull ByteBuffer buf) throws DecodeException;
}