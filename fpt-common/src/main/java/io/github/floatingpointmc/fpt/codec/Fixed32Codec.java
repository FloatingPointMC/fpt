package io.github.floatingpointmc.fpt.codec;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class Fixed32Codec extends Codec<Integer> {
    public static final Fixed32Codec INSTANCE = new Fixed32Codec();

    private Fixed32Codec() {
        super("fpt:int:fixed32");
    }

    @Override
    public void encode(@NotNull ByteBuffer buf, Integer value) {
        buf.putInt(value);
    }

    @Override
    public Integer decode(@NotNull ByteBuffer buf) {
        return buf.getInt();
    }
}