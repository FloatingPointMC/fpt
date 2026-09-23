package io.github.floatingpointmc.fpirc.demo;

import io.github.floatingpointmc.fpirc.server.api.FPIRCServer;

public class DynamicPortDemo {

    public static void main(String[] args) throws InterruptedException {
        FPIRCServer server = FPIRCServer.run("127.0.0.1", 0);
        System.out.println("[Demo] Server started with port=0, actual port: " + server.getPort());
        System.out.println("[Demo] Server is running: " + server.isRunning());

        if (server.getPort() <= 0) {
            System.err.println("[Demo] ERROR: Dynamic port allocation failed!");
        } else {
            System.out.println("[Demo] Dynamic port allocation succeeded! Port=" + server.getPort());
        }

        Thread.sleep(500);
        server.stop();
        System.out.println("[Demo] Server stopped. Running: " + server.isRunning());

        FPIRCServer server2 = FPIRCServer.run("127.0.0.1", 0);
        System.out.println("[Demo] Second server started, actual port: " + server2.getPort());
        Thread.sleep(500);
        server2.stop();
        System.out.println("[Demo] Second server stopped.");
    }
}