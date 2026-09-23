package io.github.floatingpointmc.fpirc.server.api;

import io.github.floatingpointmc.fpirc.server.core.ServerLifecycle;

public final class FPIRCServer {

    private final ServerLifecycle lifecycle;

    private FPIRCServer(String host, int port) {
        this.lifecycle = new ServerLifecycle(host, port);
    }

    public static FPIRCServer run(String host, int port) {
        FPIRCServer server = new FPIRCServer(host, port);
        server.lifecycle.start();
        return server;
    }

    public void stop() {
        lifecycle.stop();
    }

    public String getHost() {
        return lifecycle.getHost();
    }

    public int getPort() {
        return lifecycle.getPort();
    }

    public boolean isRunning() {
        return lifecycle.getState() == ServerState.RUNNING;
    }

    public ServerState getState() {
        return lifecycle.getState();
    }
}