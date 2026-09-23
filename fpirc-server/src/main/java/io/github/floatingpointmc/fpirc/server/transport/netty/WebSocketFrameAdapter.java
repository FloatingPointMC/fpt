package io.github.floatingpointmc.fpirc.server.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public final class WebSocketFrameAdapter extends MessageToMessageDecoder<WebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) {
        if (frame instanceof BinaryWebSocketFrame binaryFrame) {
            ByteBuf content = binaryFrame.content().retain();
            out.add(content);
        }
    }
}