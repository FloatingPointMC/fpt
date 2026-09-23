package io.github.floatingpointmc.fpirc.server.transport.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.server.connection.Connection;
import io.github.floatingpointmc.fpirc.server.connection.MessageSender;
import io.github.floatingpointmc.fpirc.server.handler.MessageDispatcher;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = Logger.getLogger(NettyServerHandler.class.getName());

    private final MessageDispatcher messageDispatcher;
    private volatile Connection connection;

    public NettyServerHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        connection = new Connection(remoteAddress);
        connection.setMessageSender(new NettyMessageSender(ctx));
        messageDispatcher.onConnectionActive(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (connection != null) {
            messageDispatcher.onConnectionInactive(connection);
            connection = null;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            if (connection != null) {
                messageDispatcher.dispatch(message, connection);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            LOGGER.fine("Idle connection, closing: " + ctx.channel().remoteAddress());
            ctx.close();
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.log(Level.WARNING, "Exception in channel " + ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NettyMessageSender implements MessageSender {
        private final ChannelHandlerContext ctx;

        @Override
        public void send(Message message) {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(message);
            }
        }

        @Override
        public void close() {
            ctx.close();
        }
    }
}