package io.github.floatingpointmc.fpt.transport;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMessenger implements Messenger {
    protected @Nullable Channel channel;

    @Override
    public final void onConnectionActive(@NotNull Channel channel) {
        this.channel = channel;
    }

    @Override
    public final void onConnectionInactive(@NotNull Channel channel) {
        this.channel = null;
    }

    @Override
    public final void send(@NotNull Message message) {
        if (channel != null) {
            channel.writeAndFlush(message);
        }
    }
}
