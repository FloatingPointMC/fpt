package io.github.floatingpointmc.fpirc.demo;

import io.github.floatingpointmc.fpirc.client.api.FPIRCClient;
import io.github.floatingpointmc.fpirc.common.protocol.message.ChatMessage;
import io.github.floatingpointmc.fpirc.common.protocol.message.LoginMessage;

public class ClientDemo {

    public static void main(String[] args) throws InterruptedException {
        FPIRCClient client = FPIRCClient.connect("ws://127.0.0.1:25565");

        System.out.println("Client connected: " + client.isConnected());

        client.send(new LoginMessage("DemoUser"));
        Thread.sleep(100);

        client.send(new ChatMessage("#general", "Hello FPIRC!"));
        Thread.sleep(100);

        System.out.println("Client still connected: " + client.isConnected());

        Thread.sleep(2000);

        client.disconnect();
        System.out.println("Client connected after disconnect: " + client.isConnected());
    }
}