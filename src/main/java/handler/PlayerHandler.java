package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import model.GameState;
import model.Player;
import model.Round;
import server.network.GameServer;

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
            // ✅ Read username (sent by client)
            String username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                System.out.println("Invalid username, closing connection");
                return;
            }
            
            username = username.trim();
            player = new Player(username);
            gameState.addPlayer(player);

            System.out.println("✅ Player connected: " + username);
            server.broadcast(username + " has joined the game!");

            // Start first round if needed
            if (gameState.getCurrentRound() == null) {
                gameState.startNextRound();
                Round currentRound = gameState.getCurrentRound();
                
                // Send human-readable message
                server.broadcast("New round started! Drawer: " + currentRound.getDrawer().getUsername());
                
                // ✅ Send structured message for client parsing
                String roundMsg = "ROUND_STARTED:" + currentRound.getRoundNumber() + "," + currentRound.getDrawer().getUsername();
                server.broadcast(roundMsg);
                System.out.println("✅ Broadcasted: " + roundMsg);
                
                gameState.notifyRoundStarted(currentRound);
            } else {
                // ✅ Send current round info to newly connected player
                Round currentRound = gameState.getCurrentRound();
                String roundMsg = "ROUND_STARTED:" + currentRound.getRoundNumber() + "," + currentRound.getDrawer().getUsername();
                sendMessage(roundMsg);
                System.out.println("✅ Sent current round to " + username + ": " + roundMsg);
                
                // Send existing drawing data
                for (String stroke : currentRound.getDrawingData()) {
                    sendMessage("STROKE:" + stroke);
                }
            }

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from " + username + ": " + message);

                // Handle GUESS
                if (message.startsWith("GUESS:")) {
                    String guess = message.substring(6).trim();
                    handleGuess(guess);
                } 
                // Handle STROKE
                else if (message.startsWith("STROKE:")) {
                    String strokeData = message.substring(7).trim();
                    handleStroke(strokeData);
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleGuess(String guess) {
        Round currentRound = gameState.getCurrentRound();
        if (currentRound == null) {
            return;
        }

        // Don't allow drawer to guess
        if (player.equals(currentRound.getDrawer())) {
            sendMessage("You are the drawer! You cannot guess.");
            return;
        }

        // Broadcast the guess to everyone
        server.broadcast(player.getUsername() + " guessed: " + guess);

        boolean correct = gameState.checkGuess(guess);
        if (correct) {
            player.addScore(10);
            server.broadcast(player.getUsername() + " guessed the word correctly! ✅");

            // Start new round
            gameState.startNextRound();
            Round newRound = gameState.getCurrentRound();
            
            server.broadcast("New round started! Drawer: " + newRound.getDrawer().getUsername());
            String roundMsg = "ROUND_STARTED:" + newRound.getRoundNumber() + "," + newRound.getDrawer().getUsername();
            server.broadcast(roundMsg);
            System.out.println("✅ New round broadcasted: " + roundMsg);

            gameState.notifyRoundStarted(newRound);
        } else {
            sendMessage("Wrong guess: " + guess);
        }

        gameState.notifyGuessResult(player, correct);
    }

    private void handleStroke(String strokeData) {
        Round currentRound = gameState.getCurrentRound();
        if (currentRound == null) {
            return;
        }

        // ✅ Verify this player is the drawer
        if (!player.equals(currentRound.getDrawer())) {
            System.out.println("❌ " + player.getUsername() + " tried to draw but is not the drawer!");
            sendMessage("You are not the drawer!");
            return;
        }

        System.out.println("✅ Drawer " + player.getUsername() + " sent stroke: " + strokeData);
        currentRound.addDrawingStroke(strokeData);

        // ✅ Broadcast to everyone EXCEPT the drawer
        for (PlayerHandler client : server.getClients()) {
            if (!client.equals(this)) {
                client.sendMessage("STROKE:" + strokeData);
            }
        }

        gameState.notifyDrawingUpdated(currentRound);
    }

    private void disconnect() {
        try {
            if (player != null) {
                gameState.removePlayer(player);
                server.removeClient(this);
                server.broadcast(player.getUsername() + " disconnected.");
                System.out.println(player.getUsername() + " disconnected.");
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}