package handler;  // if you want it in the same package as GameServer

import model.GameState;
import model.Player;
import server.network.GameServer;

import java.io.*;
import java.net.Socket;

public class PlayerHandler implements Runnable {

    private Socket socket;
    private GameState gameState;
    private GameServer server;
    private Player player;

    private BufferedReader in;
    private PrintWriter out;

    public PlayerHandler(Socket socket, GameState gameState, GameServer server) {
        this.socket = socket;
        this.gameState = gameState;
        this.server = server;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Ask for username
            out.println("Enter your username:");
            String username = in.readLine();
            player = new Player(username);
            gameState.addPlayer(player);

            server.broadcast(username + " has joined the game!");

            // Main loop: receive messages from client
            String message;
            while ((message = in.readLine()) != null) {
                // For now, just print the received message
                System.out.println(player.getUsername() + ": " + message);

                // You can later process guesses or drawing data here
                // Example: if message starts with "GUESS:", check guess
                if (message.startsWith("GUESS:")) {
                    String guess = message.substring(6).trim();
                    if (gameState.checkGuess(guess)) {
                        server.broadcast(player.getUsername() + " guessed the word correctly!");
                        gameState.startNextRound();
                        server.broadcast("New round started! Drawer: " +
                                gameState.getCurrentRound().getDrawer().getUsername());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                gameState.getPlayers().remove(player); // remove from game state
                server.removeClient(this);
                server.broadcast(player.getUsername() + " disconnected.");
                System.out.println(player.getUsername() + " disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send message to this client
    public void sendMessage(String message) {
        out.println(message);
    }
}
