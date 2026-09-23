package io.github.floatingpointmc.fpirc.server.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.common.protocol.MessageCoder;
import io.github.floatingpointmc.fpirc.common.protocol.MessageDecodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NettyMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger LOGGER = Logger.getLogger(NettyMessageDecoder.class.getName());

    private final MessageRegistry messageRegistry;

    public NettyMessageDecoder(MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        try {
            Message message = MessageCoder.decode(messageRegistry, buffer);
            out.add(message);
        } catch (MessageDecodeException e) {
            LOGGER.log(Level.WARNING, "Failed to decode message from " + ctx.channel().remoteAddress(), e);
            ctx.close();
        }
    }
}