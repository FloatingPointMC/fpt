package io.github.floatingpointmc.fpirc.server.connection;

import io.github.floatingpointmc.fpirc.common.protocol.S2CMessage;
import io.github.floatingpointmc.fpirc.server.session.Session;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicLong;

public final class Connection {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    @Getter
    private final long id;
    @Getter
    private final String remoteAddress;
    @Setter
    private volatile MessageSender messageSender;
    @Setter
    @Getter
    private volatile Session session;

    public Connection(String remoteAddress) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.remoteAddress = remoteAddress;
    }

    public void send(S2CMessage message) {
        MessageSender sender = messageSender;
        if (sender != null) {
            sender.send(message);
        }
    }

    public void disconnect() {
        MessageSender sender = messageSender;
        if (sender != null) {
            sender.close();
        }
    }

    @Override
    public String toString() {
        return "Connection{id=" + id + ", remote=" + remoteAddress + "}";
    }
}