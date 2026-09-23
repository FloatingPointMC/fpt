package io.github.floatingpointmc.fpirc.server.connection;

import io.github.floatingpointmc.fpirc.common.protocol.Message;
import io.github.floatingpointmc.fpirc.server.session.Session;

import java.util.concurrent.atomic.AtomicLong;

public final class Connection {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private final long id;
    private final String remoteAddress;
    private volatile MessageSender messageSender;
    private volatile Session session;

    public Connection(String remoteAddress) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.remoteAddress = remoteAddress;
    }

    public long getId() {
        return id;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setMessageSender(MessageSender sender) {
        this.messageSender = sender;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void send(Message message) {
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