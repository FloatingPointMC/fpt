package io.github.floatingpointmc.fpirc.server.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;

public final class NettyFrameDecoder extends ByteToMessageDecoder {

    private final int maxPacketSize;

    public NettyFrameDecoder(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        VarIntResult lengthResult = tryReadVarInt(in);
        if (!lengthResult.complete) {
            in.resetReaderIndex();
            return;
        }

        int length = lengthResult.value;
        if (length < 0) {
            throw new IllegalArgumentException("Negative packet length: " + length);
        }
        if (length > maxPacketSize) {
            throw new IllegalArgumentException("Packet too large: " + length + " > " + maxPacketSize);
        }

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        ByteBuf frame = in.readRetainedSlice(length);
        out.add(frame);
    }

    private VarIntResult tryReadVarInt(ByteBuf buf) {
        int value = 0;
        int position = 0;
        int bytesRead = 0;

        while (true) {
            if (!buf.isReadable()) {
                return new VarIntResult(false, 0);
            }
            byte currentByte = buf.readByte();
            bytesRead++;
            value |= (currentByte & 0x7F) << position;
            if ((currentByte & 0x80) == 0) {
                return new VarIntResult(true, value);
            }
            position += 7;
            if (position >= 32) {
                throw new IllegalArgumentException("VarInt too big");
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class VarIntResult {
        private final boolean complete;
        private final int value;
    }
}