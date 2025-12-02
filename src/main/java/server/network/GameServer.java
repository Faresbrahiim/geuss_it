package server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import handler.PlayerHandler;
import model.GameState;
import model.Player;

public class GameServer {
    // final since we will use it only once (affect only one value)
    // port where the server listens
    private final int port;
    private GameState gameState; // core logic of game 
    private final List<PlayerHandler> clients; // clients (players)

    public GameServer(int port) {
        this.port = port;
        this.gameState = new GameState(); // instance
        this.clients = new ArrayList<>(); // start empty
    }

    public void start() {
        // open socket 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(" Game server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(" New client connected: " + clientSocket.getInetAddress());

                PlayerHandler handler = new PlayerHandler(clientSocket, gameState, this);
                clients.add(handler);
                // Start the PlayerHandler in its own thread
                // The server does not freeze while waiting for one player.
                // server is the main thread
                // any other loop inside the handler will execute in mini program + execute run method
                new Thread(handler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // brodcast the msg to clients
    public void broadcast(String message) {
        for (PlayerHandler client : clients) {
            client.sendMessage(message);
        }
    }
    // brodcast the msg exept the current client 
    public void broadcast(String message, PlayerHandler excludeClient) {
        for (PlayerHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }
    // send the score to plater
    public void sendScoreUpdate(Player player) {
        for (PlayerHandler client : clients) {
            if (client.getPlayer().equals(player)) {
                client.sendMessage("SCORE_UPDATE:" + player.getScore());
                break;
            }
        }
    }
    // get clients
    public List<PlayerHandler> getClients() {
        return clients;
    }
    // remove clients
    public void removeClient(PlayerHandler client) {
        clients.remove(client);
    }
    
    // public static void main(String[] args) {
    //     GameServer server = new GameServer(5000);
    //     server.start();
    // }
}