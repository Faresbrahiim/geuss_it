package server.network;
import handler.PlayerHandler;


import handler.PlayerHandler;
import model.GameState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    private final int port;
    private GameState gameState;
    private List<PlayerHandler> clients; // keep track of connected clients
    // in game server will need just  the port to launch the socket
    public GameServer(int port) {
        this.port = port;
        // logic of game etc....
        this.gameState = new GameState();
        // currrent connected client
        this.clients = new ArrayList<>();
    }
    // start the server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // means server started sucssesfulliii
            System.out.println("Game server started on port " + port);
            // infinite for loop for
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a handler for the new client
                PlayerHandler handler = new PlayerHandler(clientSocket, gameState, this);
                clients.add(handler);

                // Start the handler thread
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast a message to all clients
    public void broadcast(String message) {
        for (PlayerHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // Remove a client (on disconnect)
    public void removeClient(PlayerHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(5000); // you can choose any port
        server.start();
    }
}
