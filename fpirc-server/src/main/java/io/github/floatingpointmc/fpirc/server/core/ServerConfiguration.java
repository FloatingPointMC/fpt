package io.github.floatingpointmc.fpirc.server.core;

import lombok.Getter;

@Getter
public final class ServerConfiguration {

    private final String host;
    private final int port;
    private final int maxPacketSize;
    private final int maxFrameLength;

    public ServerConfiguration(String host, int port) {
        this.host = host;
        this.port = port;
        this.maxPacketSize = 1 << 20;
        this.maxFrameLength = maxPacketSize + 5;
    }
}