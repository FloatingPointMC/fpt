package io.github.floatingpointmc.fpirc.client.api;

import io.github.floatingpointmc.fpirc.client.core.ClientLifecycle;
import io.github.floatingpointmc.fpirc.common.protocol.C2SMessage;

public final class FPIRCClient {

    private final ClientLifecycle lifecycle;

    private FPIRCClient(String url) {
        this.lifecycle = new ClientLifecycle(url);
    }

    public static FPIRCClient connect(String url) {
        FPIRCClient client = new FPIRCClient(url);
        client.lifecycle.connect();
        return client;
    }

    public void send(C2SMessage message) {
        lifecycle.send(message);
    }

    public void disconnect() {
        lifecycle.disconnect();
    }

    public boolean isConnected() {
        return lifecycle.getState() == ClientState.CONNECTED;
    }

    public ClientState getState() {
        return lifecycle.getState();
    }
}