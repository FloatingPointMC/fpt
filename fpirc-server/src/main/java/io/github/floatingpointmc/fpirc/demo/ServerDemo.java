package io.github.floatingpointmc.fpirc.demo;

import io.github.floatingpointmc.fpirc.server.api.FPIRCServer;

public class ServerDemo {

    public static void main(String[] args) {
        FPIRCServer server = FPIRCServer.run("0.0.0.0", 25565);

        System.out.println("FPIRC Server started on " + server.getHost() + ":" + server.getPort());
        System.out.println("Server is running: " + server.isRunning());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down FPIRC Server...");
            server.stop();
            System.out.println("Server stopped. Running: " + server.isRunning());
        }));
    }
}