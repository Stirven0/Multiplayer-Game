package com.aa.server;
import com.aa.server.network.GameServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        GameServer server = new GameServer(new InetSocketAddress(port));
        server.start();
        System.out.println("[MAIN] Server iniciado");

        // Mantener vivo
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[MAIN] Server apagado");
            try {
                server.stop(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
}
