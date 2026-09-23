package io.github.floatingpointmc.fpirc.client.transport.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.github.floatingpointmc.fpirc.client.handler.MessageDispatcher;
import io.github.floatingpointmc.fpirc.common.protocol.Message;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = Logger.getLogger(NettyClientHandler.class.getName());

    private final MessageDispatcher messageDispatcher;

    public NettyClientHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOGGER.info("Connected to server: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info("Disconnected from server: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            messageDispatcher.dispatch(message);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.log(Level.WARNING, "Exception in client channel", cause);
        ctx.close();
    }
}