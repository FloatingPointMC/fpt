package io.github.floatingpointmc.fpt.transport.netty;

import io.github.floatingpointmc.fpt.codec.exceptions.EncodeException;
import io.github.floatingpointmc.fpt.codec.MessageCoder;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class NettyMessageEncoder extends MessageToMessageEncoder<Message> {
    private static final Logger LOGGER = Logger.getLogger(NettyMessageEncoder.class.getName());

    private final @NotNull Protocol protocol;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        try {
            byte[] packet = MessageCoder.encode(protocol, msg);
            ByteBuf encoded = Unpooled.wrappedBuffer(packet);
            out.add(encoded);
        } catch (EncodeException e) {
            LOGGER.log(Level.WARNING, "Failed to encode message: " + msg.getClass().getSimpleName(), e);
        }
    }
}