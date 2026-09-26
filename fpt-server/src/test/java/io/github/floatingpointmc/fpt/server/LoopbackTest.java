package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.client.FPTClient;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.message.impl.S2CMessage;
import io.github.floatingpointmc.fpt.transport.AbstractMessenger;
import io.github.floatingpointmc.fpt.transport.Messenger;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class LoopbackTest {

    public static class ChatMessage implements C2SMessage {
        public String text;
    }

    public static class ChatResponse implements S2CMessage {
        public String text;
    }

    @Test
    void clientConnectsAndDisconnects() {
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
    void defaultProtocolConnection() {
        FPTServer server = FPTServer.create("0.0.0.0", 0);
        server.run();
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.create("127.0.0.1", port);
            client.connect();
            assertTrue(client.isConnected());
            client.disconnect();
        } finally {
            server.stop();
        }
    }

    @Test
    void loopbackCommunication() throws Exception {
        Protocol protocol = Protocol.create()
                .registerC2S(ChatMessage.class)
                .registerS2C(ChatResponse.class);

        List<Message> receivedOnServer = new ArrayList<>();
        CountDownLatch serverReceived = new CountDownLatch(1);

        Messenger serverMessenger = new Messenger() {
            @Override
            public void onConnectionActive(@NotNull Channel channel) {
            }

            @Override
            public void onConnectionInactive(@NotNull Channel channel) {
            }

            @Override
            public void onMessage(@NotNull Message message) {
                receivedOnServer.add(message);
                serverReceived.countDown();
            }

            @Override
            public void send(@NotNull Message message) {
            }
        };

        FPTServer server = FPTServer.create("127.0.0.1", 0, protocol);
        server.messenger(serverMessenger);
        server.run();
        int port = server.getPort();

        try {
            AbstractMessenger clientMessenger = new AbstractMessenger() {
                @Override
                public void onMessage(@NotNull Message message) {
                }
            };

            FPTClient client = FPTClient.create("127.0.0.1", port, protocol);
            client.messenger(clientMessenger);
            client.connect();
            assertTrue(client.isConnected());

            ChatMessage msg = new ChatMessage();
            msg.text = "hello";
            clientMessenger.send(msg);

            assertTrue(serverReceived.await(5, TimeUnit.SECONDS), "Server should receive message");
            assertEquals(1, receivedOnServer.size());
            assertInstanceOf(ChatMessage.class, receivedOnServer.get(0));
            assertEquals("hello", ((ChatMessage) receivedOnServer.get(0)).text);

            client.disconnect();
        } finally {
            server.stop();
        }
    }

    @Test
    void handshakeMismatchDisconnects() {
        Protocol serverProtocol = Protocol.create()
                .registerC2S(ChatMessage.class);

        Protocol clientProtocol = Protocol.create();

        FPTServer server = FPTServer.create("127.0.0.1", 0, serverProtocol);
        server.run();
        int port = server.getPort();

        try {
            FPTClient client = FPTClient.create("127.0.0.1", port, clientProtocol);
            assertThrows(RuntimeException.class, client::connect);
        } finally {
            server.stop();
        }
    }

    @Test
    void multipleClientsConnectAndDisconnect() {
        FPTServer server = FPTServer.create("0.0.0.0", 0);
        server.run();
        int port = server.getPort();

        try {
            int clientCount = 3;
            List<FPTClient> clients = new ArrayList<>();
            for (int i = 0; i < clientCount; i++) {
                FPTClient client = FPTClient.create("127.0.0.1", port);
                client.connect();
                assertTrue(client.isConnected());
                clients.add(client);
            }
            for (FPTClient client : clients) {
                client.disconnect();
                assertFalse(client.isConnected());
            }
        } finally {
            server.stop();
        }
    }
}