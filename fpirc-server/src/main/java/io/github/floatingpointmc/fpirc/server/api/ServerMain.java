package io.github.floatingpointmc.fpirc.server.api;

public final class ServerMain {

    private ServerMain() {
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "0.0.0.0";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 25565;

        FPIRCServer server = FPIRCServer.run(host, port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down FPIRC Server...");
            server.stop();
        }));

        System.out.println("FPIRC Server running on " + server.getHost() + ":" + server.getPort());
    }
}