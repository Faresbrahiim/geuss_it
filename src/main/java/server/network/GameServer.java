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

    private final int port;
    private GameState gameState;
    private final List<PlayerHandler> clients;

    public GameServer(int port) {
        this.port = port;
        this.gameState = new GameState();
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🎮 Game server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✅ New client connected: " + clientSocket.getInetAddress());

                PlayerHandler handler = new PlayerHandler(clientSocket, gameState, this);
                clients.add(handler);

                new Thread(handler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (PlayerHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void broadcast(String message, PlayerHandler excludeClient) {
        for (PlayerHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public void sendScoreUpdate(Player player) {
        for (PlayerHandler client : clients) {
            if (client.getPlayer().equals(player)) {
                client.sendMessage("SCORE_UPDATE:" + player.getScore());
                break;
            }
        }
    }

    public List<PlayerHandler> getClients() {
        return clients;
    }

    public void removeClient(PlayerHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(5000);
        server.start();
    }
}