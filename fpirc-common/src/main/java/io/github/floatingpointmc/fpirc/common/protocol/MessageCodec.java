package io.github.floatingpointmc.fpirc.common.protocol;

import java.nio.ByteBuffer;

public interface MessageCodec<T extends Message> {

    T decode(ByteBuffer buffer) throws MessageDecodeException;

    void encode(T message, ByteBuffer buffer) throws MessageEncodeException;
}