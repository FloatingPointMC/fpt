package io.github.floatingpointmc.fpirc.server.connection;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectionManager {

    private final Map<Long, Connection> connections = new ConcurrentHashMap<>();

    public ConnectionManager() {
    }

    public void add(Connection connection) {
        connections.put(connection.getId(), connection);
    }

    public void remove(long connectionId) {
        connections.remove(connectionId);
    }

    public Connection get(long connectionId) {
        return connections.get(connectionId);
    }

    public Collection<Connection> getAll() {
        return connections.values();
    }

    public int count() {
        return connections.size();
    }

    public void closeAll() {
        for (Connection connection : connections.values()) {
            connection.disconnect();
        }
        connections.clear();
    }
}