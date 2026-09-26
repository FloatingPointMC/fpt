package io.github.floatingpointmc.fpt.transport;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventGroup implements AutoCloseable {
    private final @NotNull EventLoopGroup bossGroup;
    private final @NotNull EventLoopGroup workerGroup;
    private final boolean owned;

    public static @NotNull EventGroup nio() {
        EventLoopGroup boss = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        EventLoopGroup worker = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        return new EventGroup(boss, worker, true);
    }

    public static @NotNull EventGroup wrap(@NotNull EventLoopGroup boss, @NotNull EventLoopGroup worker) {
        return new EventGroup(boss, worker, false);
    }

    public @NotNull EventLoopGroup bossGroup() {
        return bossGroup;
    }

    public @NotNull EventLoopGroup workerGroup() {
        return workerGroup;
    }

    @Override
    public void close() {
        if (owned) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}