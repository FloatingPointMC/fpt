package io.github.floatingpointmc.fpirc.server.core;

import io.github.floatingpointmc.fpirc.common.protocol.DefaultMessageRegistry;
import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;
import io.github.floatingpointmc.fpirc.server.api.ServerState;
import io.github.floatingpointmc.fpirc.server.connection.ConnectionManager;
import io.github.floatingpointmc.fpirc.server.handler.MessageDispatcher;
import io.github.floatingpointmc.fpirc.server.transport.netty.NettyServer;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

public final class ServerLifecycle {
    @Getter
    private final String host;
    private final int port;
    private final AtomicReference<ServerState> state = new AtomicReference<>(ServerState.NEW);
    private final MessageRegistry c2sRegistry;
    private final MessageRegistry s2cRegistry;
    @Getter
    private final ConnectionManager connectionManager;
    private final MessageDispatcher messageDispatcher;
    private volatile NettyServer nettyServer;

    public ServerLifecycle(String host, int port) {
        this.host = host;
        this.port = port;
        this.c2sRegistry = DefaultMessageRegistry.createC2S();
        this.s2cRegistry = DefaultMessageRegistry.createS2C();
        this.connectionManager = new ConnectionManager();
        this.messageDispatcher = new MessageDispatcher(connectionManager);
    }

    public void start() {
        if (!state.compareAndSet(ServerState.NEW, ServerState.STARTING)) {
            throw new IllegalStateException("Server cannot be started from state: " + state.get());
        }

        try {
            nettyServer = new NettyServer(host, port, c2sRegistry, s2cRegistry, connectionManager, messageDispatcher);
            nettyServer.start();
            state.set(ServerState.RUNNING);
        } catch (Exception e) {
            state.set(ServerState.STOPPED);
            throw new RuntimeException("Failed to start server", e);
        }
    }

    public void stop() {
        ServerState current = state.get();
        if (current == ServerState.STOPPED || current == ServerState.STOPPING) {
            return;
        }
        if (!state.compareAndSet(current, ServerState.STOPPING)) {
            return;
        }

        try {
            if (nettyServer != null) {
                nettyServer.stop();
            }
            connectionManager.closeAll();
        } finally {
            state.set(ServerState.STOPPED);
        }
    }

    public int getPort() {
        if (nettyServer != null) {
            return nettyServer.getActualPort();
        }
        return port;
    }

    public ServerState getState() {
        return state.get();
    }
}