package io.github.floatingpointmc.fpt.server;

import io.github.floatingpointmc.fpt.client.FPTClient;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.message.impl.S2CMessage;
import io.github.floatingpointmc.fpt.transport.AbstractMessenger;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.Messenger;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoopbackTest {

    public static class ChatMessage implements C2SMessage {
        public String text;
    }

    @SuppressWarnings("unused")
    public static class ChatResponse implements S2CMessage {
        public String text;
    }

    @Test
    void serverStartsAndStops() {
        FPTServer server = FPTServer.run("127.0.0.1", 0);
        assertTrue(server.isRunning());
        assertTrue(server.getPort() > 0);
        server.stop();
    }

    @Test
    void clientConnectsAndDisconnects() {
        FPTServer server = FPTServer.run("0.0.0.0", 0);
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.connect("127.0.0.1", port);
            assertTrue(client.isConnected());
            client.disconnect();
            assertFalse(client.isConnected());
        } finally {
            server.stop();
        }
    }

    @Test
    void loopbackCommunication() throws Exception {
        Protocol protocol = Protocol.create()
                .registerC2S(ChatMessage.class)
                .registerS2C(ChatResponse.class);

        final List<Message> receivedOnServer = new ArrayList<>();
        final CountDownLatch serverReceived = new CountDownLatch(1);
        Messenger serverListener = getServerListener(receivedOnServer, serverReceived);

        EventGroup serverEventGroup = EventGroup.nio();
        FPTServer server = FPTServer.run("127.0.0.1", 0, protocol, serverEventGroup, serverListener);
        int port = server.getPort();

        try {
            Messenger messenger = new AbstractMessenger() {
                @Override
                public void onMessage(@NotNull Message message, @NotNull Channel channel) {

                }
            };
            FPTClient client = FPTClient.connect("127.0.0.1", port, protocol, EventGroup.nio(), messenger);
            assertTrue(client.isConnected());

            ChatMessage msg = new ChatMessage();
            msg.text = "hello";
            messenger.send(msg);

            assertTrue(serverReceived.await(5, TimeUnit.SECONDS), "Server should receive message");
            assertEquals(1, receivedOnServer.size());
            assertInstanceOf(ChatMessage.class, receivedOnServer.get(0));
            assertEquals("hello", ((ChatMessage) receivedOnServer.get(0)).text);

            client.disconnect();
        } finally {
            server.stop();
        }
    }

    private static Messenger getServerListener(List<Message> receivedOnServer, CountDownLatch serverReceived) {
        return new Messenger() {
            @Override
            public void onConnectionActive(@NotNull Channel channel) {
            }

            @Override
            public void onConnectionInactive(@NotNull Channel channel) {
            }

            @Override
            public void onMessage(@NotNull Message message, @NotNull Channel channel) {
                receivedOnServer.add(message);
                serverReceived.countDown();
            }

            @Override
            public void send(@NotNull Message message) {

            }
        };
    }

    @Test
    void handshakeMismatchDisconnects() {
        Protocol serverProtocol = Protocol.create()
                .registerC2S(ChatMessage.class);

        Protocol clientProtocol = Protocol.create();

        FPTServer server = FPTServer.run("127.0.0.1", 0, serverProtocol);
        int port = server.getPort();

        try {
            assertThrows(RuntimeException.class, () -> FPTClient.connect("127.0.0.1", port, clientProtocol));
        } finally {
            server.stop();
        }
    }

    @Test
    void defaultProtocolAPI() {
        FPTServer server = FPTServer.run("127.0.0.1", 0);
        int port = server.getPort();
        try {
            FPTClient client = FPTClient.connect("127.0.0.1", port);
            assertTrue(client.isConnected());
            client.disconnect();
        } finally {
            server.stop();
        }
    }
}