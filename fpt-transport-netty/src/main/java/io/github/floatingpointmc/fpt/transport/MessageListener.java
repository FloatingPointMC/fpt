package io.github.floatingpointmc.fpt.transport;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

public interface MessageListener {
    void onConnectionActive(@NotNull Channel channel);

    void onConnectionInactive(@NotNull Channel channel);

    void onMessage(@NotNull Message message, @NotNull Channel channel);
}