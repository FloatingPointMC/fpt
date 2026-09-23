package io.github.floatingpointmc.fpirc.client.core;

import io.github.floatingpointmc.fpirc.client.api.ClientState;
import io.github.floatingpointmc.fpirc.client.handler.MessageDispatcher;
import io.github.floatingpointmc.fpirc.client.transport.netty.NettyClient;
import io.github.floatingpointmc.fpirc.common.protocol.DefaultMessageRegistry;
import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.common.protocol.MessageRegistry;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

public final class ClientLifecycle {
    private final String url;
    private final AtomicReference<ClientState> state = new AtomicReference<>(ClientState.NEW);
    @Getter
    private final MessageRegistry messageRegistry;
    private final MessageDispatcher messageDispatcher;
    private volatile NettyClient nettyClient;

    public ClientLifecycle(String url) {
        this.url = url;
        this.messageRegistry = DefaultMessageRegistry.create();
        this.messageDispatcher = new MessageDispatcher();
    }

    public void connect() {
        if (!state.compareAndSet(ClientState.NEW, ClientState.CONNECTING)) {
            throw new IllegalStateException("Client cannot connect from state: " + state.get());
        }

        try {
            nettyClient = new NettyClient(url, messageRegistry, messageDispatcher);
            nettyClient.connect();
            state.set(ClientState.CONNECTED);
        } catch (Exception e) {
            state.set(ClientState.DISCONNECTED);
            throw new RuntimeException("Failed to connect to " + url, e);
        }
    }

    public void send(Message message) {
        if (state.get() != ClientState.CONNECTED) {
            throw new IllegalStateException("Client is not connected");
        }
        if (nettyClient != null) {
            nettyClient.send(message);
        }
    }

    public void disconnect() {
        ClientState current = state.get();
        if (current == ClientState.DISCONNECTED || current == ClientState.DISCONNECTING) {
            return;
        }
        if (!state.compareAndSet(current, ClientState.DISCONNECTING)) {
            return;
        }

        try {
            if (nettyClient != null) {
                nettyClient.disconnect();
            }
        } finally {
            state.set(ClientState.DISCONNECTED);
        }
    }

    public ClientState getState() {
        return state.get();
    }
}