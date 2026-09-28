package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.client.ClientHandler;
import io.github.floatingpointmc.fpt.client.FPTClientFactory;
import io.github.floatingpointmc.fpt.client.FPTClient;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.Connection;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest {

    @Test
    void serverCreateThenRun() {
        FPTServer server = FPTServerFactory.create("127.0.0.1", 0).run();
        assertTrue(server.isRunning());
        assertTrue(server.getPort() > 0);
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    void serverRunTwiceRejected() {
        FPTServerFactory server = FPTServerFactory.create("127.0.0.1", 0);
        FPTServer runtime = server.run();
        try {
            assertThrows(IllegalStateException.class, server::run);
        } finally {
            runtime.stop();
        }
    }

    @Test
    void serverMessengerBeforeRun() {
        FPTServer server = FPTServerFactory.create("127.0.0.1", 0)
                .handler(new ServerHandler() {
                    @Override
                    public void onConnectionActive(@NotNull FPTServer server, @NotNull Connection connection) {

                    }

                    @Override
                    public void onConnectionInactive(@NotNull FPTServer server, @NotNull Connection connection) {

                    }

                    @Override
                    public void onMessage(@NotNull FPTServer server, @NotNull Connection connection, @NotNull Message message) {

                    }
                })
                .run();
        assertTrue(server.isRunning());
        server.stop();
    }

    @Test
    void serverMessengerAfterRunRejected() {
        FPTServerFactory server = FPTServerFactory.create("127.0.0.1", 0);
        FPTServer runtime = server.run();
        try {
            assertThrows(IllegalStateException.class, () -> server.handler(new ServerHandler() {
                @Override
                public void onConnectionActive(@NotNull FPTServer server, @NotNull Connection connection) {

                }

                @Override
                public void onConnectionInactive(@NotNull FPTServer server, @NotNull Connection connection) {

                }

                @Override
                public void onMessage(@NotNull FPTServer server, @NotNull Connection connection, @NotNull Message message) {

                }
            }));
        } finally {
            runtime.stop();
        }
    }

    @Test
    void serverStopIsIdempotent() {
        FPTServer server = FPTServerFactory.create("127.0.0.1", 0).run();
        server.stop();
        assertFalse(server.isRunning());
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    void clientCreateThenConnect() {
        FPTServer server = FPTServerFactory.create("0.0.0.0", 0).run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClientFactory.create("127.0.0.1", port).connect();
            assertTrue(client.isConnected());
            client.disconnect();
            assertFalse(client.isConnected());
        } finally {
            server.stop();
        }
    }

    @Test
    void clientConnectTwiceRejected() {
        FPTServer server = FPTServerFactory.create("0.0.0.0", 0).run();
        int port = server.getPort();
        try {
            FPTClientFactory client = FPTClientFactory.create("127.0.0.1", port);
            FPTClient runtime = client.connect();
            try {
                assertThrows(IllegalStateException.class, client::connect);
            } finally {
                runtime.disconnect();
            }
        } finally {
            server.stop();
        }
    }

    @Test
    void clientMessengerBeforeConnect() {
        FPTServer server = FPTServerFactory.create("0.0.0.0", 0).run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClientFactory.create("127.0.0.1", port)
                    .handler(new ClientHandler() {
                        @Override
                        public void onConnectionActive(@NotNull FPTClient client, @NotNull Connection connection) {

                        }

                        @Override
                        public void onConnectionInactive(@NotNull FPTClient client, @NotNull Connection connection) {

                        }

                        @Override
                        public void onMessage(@NotNull FPTClient client, @NotNull Connection connection, @NotNull Message message) {

                        }
                    })
                    .connect();
            assertTrue(client.isConnected());
            client.disconnect();
        } finally {
            server.stop();
        }
    }

    @Test
    void clientMessengerAfterConnectRejected() {
        FPTServer server = FPTServerFactory.create("0.0.0.0", 0).run();
        int port = server.getPort();
        try {
            FPTClientFactory client = FPTClientFactory.create("127.0.0.1", port);
            FPTClient runtime = client.connect();
            try {
                assertThrows(IllegalStateException.class, () -> client.handler(new ClientHandler() {
                    @Override
                    public void onConnectionActive(@NotNull FPTClient client, @NotNull Connection connection) {

                    }

                    @Override
                    public void onConnectionInactive(@NotNull FPTClient client, @NotNull Connection connection) {

                    }

                    @Override
                    public void onMessage(@NotNull FPTClient client, @NotNull Connection connection, @NotNull Message message) {

                    }
                }));
            } finally {
                runtime.disconnect();
            }
        } finally {
            server.stop();
        }
    }

    @Test
    void clientDisconnectIsIdempotent() {
        FPTServer server = FPTServerFactory.create("0.0.0.0", 0).run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClientFactory.create("127.0.0.1", port).connect();
            client.disconnect();
            assertFalse(client.isConnected());
            client.disconnect();
            assertFalse(client.isConnected());
        } finally {
            server.stop();
        }
    }

    @Test
    void serverWithCustomProtocol() {
        Protocol protocol = Protocol.create("test", 1);
        FPTServer server = FPTServerFactory.create("127.0.0.1", 0, protocol).run();
        assertTrue(server.isRunning());
        server.stop();
    }

    @Test
    void clientConnectWithCustomProtocol() {
        Protocol protocol = Protocol.create("test", 1);
        FPTServer server = FPTServerFactory.create("0.0.0.0", 0, protocol).run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClientFactory.create("127.0.0.1", port, protocol).connect();
            assertTrue(client.isConnected());
            client.disconnect();
        } finally {
            server.stop();
        }
    }

    @Test
    void serverWithCustomEventGroup() {
        EventGroup eventGroup = EventGroup.nio();
        FPTServer server = FPTServerFactory.create("127.0.0.1", 0, eventGroup).run();
        assertTrue(server.isRunning());
        server.stop();
        eventGroup.close();
    }
}