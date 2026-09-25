package io.github.floatingpointmc.fpt.server;

import com.sun.istack.internal.NotNull;
import io.github.floatingpointmc.fpt.client.FPTClient;
import io.github.floatingpointmc.fpt.protocol.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.Message;
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.S2CMessage;
import io.github.floatingpointmc.fpt.transport.EventGroup;
import io.github.floatingpointmc.fpt.transport.MessageListener;
import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LoopbackTest {

    public static class ChatMessage implements C2SMessage {
        public String text;
    }

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
    void clientConnectsAndDisconnects() throws Exception {
        FPTServer server = FPTServer.run("127.0.0.1", 0);
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
                .register(ChatMessage.class)
                .registerS2C(ChatResponse.class);

        final List<Message> receivedOnServer = new ArrayList<>();
        final CountDownLatch serverReceived = new CountDownLatch(1);
        MessageListener serverListener = getServerListener(receivedOnServer, serverReceived);

        EventGroup serverEventGroup = EventGroup.nio();
        FPTServer server = FPTServer.run("127.0.0.1", 0, protocol, serverEventGroup, serverListener);
        int port = server.getPort();

        try {
            FPTClient client = FPTClient.connect("127.0.0.1", port, protocol);
            assertTrue(client.isConnected());

            ChatMessage msg = new ChatMessage();
            msg.text = "hello";
            client.send(msg);

            assertTrue(serverReceived.await(5, TimeUnit.SECONDS), "Server should receive message");
            assertEquals(1, receivedOnServer.size());
            assertTrue(receivedOnServer.get(0) instanceof ChatMessage);
            assertEquals("hello", ((ChatMessage) receivedOnServer.get(0)).text);

            client.disconnect();
        } finally {
            server.stop();
        }
    }

    private static MessageListener getServerListener(List<Message> receivedOnServer, CountDownLatch serverReceived) {
        final AtomicReference<Channel> serverChannel = new AtomicReference<>();

        return new MessageListener() {
            @Override
            public void onConnectionActive(@NotNull Channel channel) {
                serverChannel.set(channel);
            }

            @Override
            public void onConnectionInactive(@NotNull Channel channel) {
            }

            @Override
            public void onMessage(@NotNull Message message, @NotNull Channel channel) {
                receivedOnServer.add(message);
                serverReceived.countDown();
            }
        };
    }

    @Test
    void handshakeMismatchDisconnects() throws Exception {
        Protocol serverProtocol = Protocol.create()
                .register(ChatMessage.class);

        Protocol clientProtocol = Protocol.create();

        FPTServer server = FPTServer.run("127.0.0.1", 0, serverProtocol);
        int port = server.getPort();

        try {
            assertThrows(RuntimeException.class, () -> {
                FPTClient.connect("127.0.0.1", port, clientProtocol);
            });
        } finally {
            server.stop();
        }
    }

    @Test
    void defaultProtocolAPI() throws Exception {
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