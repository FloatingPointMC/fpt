package io.github.floatingpointmc.fpt.transport;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

public interface Messenger {
    static @NotNull Messenger empty() {
        return new Messenger() {
            @Override
            public void onConnectionActive(@NotNull Channel channel) {

            }

            @Override
            public void onConnectionInactive(@NotNull Channel channel) {

            }

            @Override
            public void onMessage(@NotNull Message message, @NotNull Channel channel) {

            }

            @Override
            public void send(@NotNull Message message) {

            }
        };
    }

    void onConnectionActive(@NotNull Channel channel);

    void onConnectionInactive(@NotNull Channel channel);

    void onMessage(@NotNull Message message, @NotNull Channel channel);

    void send(@NotNull Message message);
}