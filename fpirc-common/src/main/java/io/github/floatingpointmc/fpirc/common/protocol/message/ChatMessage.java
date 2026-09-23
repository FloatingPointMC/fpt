package io.github.floatingpointmc.fpirc.common.protocol.message;

import io.github.floatingpointmc.fpirc.common.protocol.Message;

public final class ChatMessage implements Message {

    private final String channel;
    private final String content;

    public ChatMessage(String channel, String content) {
        this.channel = channel;
        this.content = content;
    }

    public String getChannel() {
        return channel;
    }

    public String getContent() {
        return content;
    }
}