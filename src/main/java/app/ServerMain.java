package app;

import server.network.GameServer;

public class ServerMain {
    public static void main(String[] args) {
        GameServer server = new GameServer(5000);
        server.start();
    }
}
