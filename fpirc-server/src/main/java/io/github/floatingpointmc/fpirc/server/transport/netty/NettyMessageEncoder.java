package io.github.floatingpointmc.fpirc.server.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.common.protocol.MessageCoder;
import io.github.floatingpointmc.fpirc.common.protocol.MessageEncodeException;
import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NettyMessageEncoder extends MessageToMessageEncoder<Message> {

    private static final Logger LOGGER = Logger.getLogger(NettyMessageEncoder.class.getName());

    private final MessageRegistry messageRegistry;

    public NettyMessageEncoder(MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        try {
            byte[] packet = MessageCoder.encode(messageRegistry, msg);
            ByteBuf encoded = Unpooled.wrappedBuffer(packet);
            out.add(encoded);
        } catch (MessageEncodeException e) {
            LOGGER.log(Level.WARNING, "Failed to encode message: " + msg.getClass().getSimpleName(), e);
        }
    }
}