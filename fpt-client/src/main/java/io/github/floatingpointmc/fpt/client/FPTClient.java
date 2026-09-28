package io.github.floatingpointmc.fpt.client;

import io.github.floatingpointmc.fpt.transport.Connection;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.logging.Logger;

@Unmodifiable
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class FPTClient {
    private static final Logger LOGGER = Logger.getLogger(FPTClient.class.getName());
    private final @NotNull Channel clientChannel;
    @Getter
    private final @NotNull Connection connection;
    private final @NotNull EventGroup eventGroup;
    private final boolean ownedEventGroup;

    public boolean isConnected() {
        return connection.isActive();
    }

    public void disconnect() {
        clientChannel.close().awaitUninterruptibly();
        if (ownedEventGroup) {
            eventGroup.close();
        }
        LOGGER.info("Disconnected");
    }
}
