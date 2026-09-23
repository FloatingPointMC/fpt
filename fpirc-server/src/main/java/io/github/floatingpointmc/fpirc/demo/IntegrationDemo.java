package io.github.floatingpointmc.fpirc.demo;

import io.github.floatingpointmc.fpirc.client.api.FPIRCClient;
import io.github.floatingpointmc.fpirc.common.protocol.message.ChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginMessage;
import io.github.floatingpointmc.fpirc.server.api.FPIRCServer;

public class IntegrationDemo {

    public static void main(String[] args) throws InterruptedException {
        FPIRCServer server = FPIRCServer.run("0.0.0.0", 25565);
        System.out.println("[Demo] Server started on " + server.getHost() + ":" + server.getPort());
        System.out.println("[Demo] Server is running: " + server.isRunning());

        Thread.sleep(500);

        try {
            FPIRCClient client = FPIRCClient.connect("ws://127.0.0.1:25565");
            System.out.println("[Demo] Client connected: " + client.isConnected());

            client.send(new LoginMessage("DemoUser"));
            Thread.sleep(200);

            client.send(new ChatMessage("#general", "Hello FPIRC!"));
            Thread.sleep(200);

            System.out.println("[Demo] Client still connected: " + client.isConnected());

            Thread.sleep(1000);

            client.disconnect();
            System.out.println("[Demo] Client connected after disconnect: " + client.isConnected());
        } catch (Exception e) {
            System.err.println("[Demo] Client error: " + e.getMessage());
            e.printStackTrace();
        }

        Thread.sleep(500);

        server.stop();
        System.out.println("[Demo] Server stopped. Running: " + server.isRunning());
    }
}