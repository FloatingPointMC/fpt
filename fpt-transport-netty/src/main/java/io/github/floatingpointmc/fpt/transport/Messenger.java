package io.github.floatingpointmc.fpt.transport;

import io.github.floatingpointmc.fpt.protocol.message.Message;
import org.jetbrains.annotations.NotNull;

public interface Messenger {
    static @NotNull Messenger empty() {
        return new Messenger() {
            @Override
            public void onConnectionActive(@NotNull Connection channel) {

            }

            @Override
            public void onConnectionInactive(@NotNull Connection channel) {

            }

            @Override
            public void onMessage(@NotNull Connection connection, @NotNull Message message) {

            }
        };
    }

    void onConnectionActive(@NotNull Connection channel);

    void onConnectionInactive(@NotNull Connection channel);

    void onMessage(@NotNull Connection connection, @NotNull Message message);
}