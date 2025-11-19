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

        // If this is the first player, start the first round
        if (gameState.getCurrentRound() == null) {
            gameState.startNextRound();
            server.broadcast("New round started! Drawer: " +
                    gameState.getCurrentRound().getDrawer().getUsername());
        }

        // Main loop: receive messages from client
        String message;
        while ((message = in.readLine()) != null) {
            System.out.println(player.getUsername() + ": " + message);

            if (message.startsWith("GUESS:")) {
                String guess = message.substring(6).trim();

                // Broadcast the guess to everyone (except the drawer)
                for (PlayerHandler client : server.getClients()) {
                    if (!client.getPlayer().equals(gameState.getCurrentRound().getDrawer())) {
                        client.sendMessage(player.getUsername() + " guessed: " + guess);
                    }
                }

                // Check if the guess is correct
                if (gameState.checkGuess(guess)) {
                    server.broadcast(player.getUsername() + " guessed the word correctly!");
                    gameState.startNextRound();
                    server.broadcast("New round started! Drawer: " +
                            gameState.getCurrentRound().getDrawer().getUsername());
                } else {
                    // Send feedback to the guessing player
                    sendMessage("Wrong guess: " + guess);
                }
            }
        }

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            socket.close();
            gameState.getPlayers().remove(player);
            server.removeClient(this);
            server.broadcast(player.getUsername() + " disconnected.");
            System.out.println(player.getUsername() + " disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Getter for player (needed for broadcasting guesses)
public Player getPlayer() {
    return player;
}


    // Send message to this client
    public void sendMessage(String message) {
        out.println(message);
    }
}
