package io.github.floatingpointmc.fpt.transport.netty;

import io.github.floatingpointmc.fpt.codec.DecodeException;
import io.github.floatingpointmc.fpt.codec.MessageCoder;
import io.github.floatingpointmc.fpt.protocol.Message;
import io.github.floatingpointmc.fpt.protocol.MessageDirection;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class NettyMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final Logger LOGGER = Logger.getLogger(NettyMessageDecoder.class.getName());

    private final @NotNull Protocol protocol;
    private final @NotNull MessageDirection direction;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        try {
            Message message = MessageCoder.decode(protocol, direction, buffer);
            out.add(message);
        } catch (DecodeException e) {
            LOGGER.log(Level.WARNING, "Failed to decode message from " + ctx.channel().remoteAddress(), e);
            ctx.close();
        }
    }
}