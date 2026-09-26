package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.client.FPTClient;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.Messenger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest {

    @Test
    void serverCreateThenRun() {
        FPTServer server = FPTServer.create("127.0.0.1", 0);
        assertFalse(server.isRunning());
        server.run();
        assertTrue(server.isRunning());
        assertTrue(server.getPort() > 0);
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    void serverRunTwiceRejected() {
        FPTServer server = FPTServer.create("127.0.0.1", 0);
        server.run();
        try {
            assertThrows(IllegalStateException.class, server::run);
        } finally {
            server.stop();
        }
    }

    @Test
    void serverMessengerBeforeRun() {
        FPTServer server = FPTServer.create("127.0.0.1", 0)
                .messenger(Messenger.empty());
        server.run();
        assertTrue(server.isRunning());
        server.stop();
    }

    @Test
    void serverMessengerAfterRunRejected() {
        FPTServer server = FPTServer.create("127.0.0.1", 0);
        server.run();
        try {
            assertThrows(IllegalStateException.class, () -> server.messenger(Messenger.empty()));
        } finally {
            server.stop();
        }
    }

    @Test
    void clientCreateThenConnect() {
        FPTServer server = FPTServer.create("0.0.0.0", 0);
        server.run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.create("127.0.0.1", port);
            client.connect();
            assertTrue(client.isConnected());
            client.disconnect();
            assertFalse(client.isConnected());
        } finally {
            server.stop();
        }
    }

    @Test
    void clientConnectTwiceRejected() {
        FPTServer server = FPTServer.create("0.0.0.0", 0);
        server.run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.create("127.0.0.1", port);
            client.connect();
            try {
                assertThrows(IllegalStateException.class, client::connect);
            } finally {
                client.disconnect();
            }
        } finally {
            server.stop();
        }
    }

    @Test
    void clientMessengerBeforeConnect() {
        FPTServer server = FPTServer.create("0.0.0.0", 0);
        server.run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.create("127.0.0.1", port)
                    .messenger(Messenger.empty());
            client.connect();
            assertTrue(client.isConnected());
            client.disconnect();
        } finally {
            server.stop();
        }
    }

    @Test
    void clientMessengerAfterConnectRejected() {
        FPTServer server = FPTServer.create("0.0.0.0", 0);
        server.run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.create("127.0.0.1", port);
            client.connect();
            try {
                assertThrows(IllegalStateException.class, () -> client.messenger(Messenger.empty()));
            } finally {
                client.disconnect();
            }
        } finally {
            server.stop();
        }
    }

    @Test
    void serverWithCustomProtocol() {
        Protocol protocol = Protocol.create("test", 1);
        FPTServer server = FPTServer.create("127.0.0.1", 0, protocol);
        server.run();
        assertTrue(server.isRunning());
        server.stop();
    }

    @Test
    void clientConnectWithCustomProtocol() {
        Protocol protocol = Protocol.create("test", 1);
        FPTServer server = FPTServer.create("0.0.0.0", 0, protocol);
        server.run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.create("127.0.0.1", port, protocol);
            client.connect();
            assertTrue(client.isConnected());
            client.disconnect();
        } finally {
            server.stop();
        }
    }

    @Test
    void serverWithCustomEventGroup() {
        EventGroup eventGroup = EventGroup.nio();
        FPTServer server = FPTServer.create("127.0.0.1", 0, eventGroup);
        server.run();
        assertTrue(server.isRunning());
        server.stop();
        eventGroup.close();
    }
}