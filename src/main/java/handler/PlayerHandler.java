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
            String username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
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
                broadcastRoundStart(currentRound);
            } else {
                // Send current round info to new player
                Round currentRound = gameState.getCurrentRound();
                sendRoundInfo(currentRound);
                
                // Send existing drawing
                for (String stroke : currentRound.getDrawingData()) {
                    sendMessage("STROKE:" + stroke);
                }
            }

            // Send initial score
            sendMessage("SCORE_UPDATE:" + player.getScore());

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from " + username + ": " + message);

                if (message.startsWith("GUESS:")) {
                    handleGuess(message.substring(6).trim());
                } else if (message.startsWith("STROKE:")) {
                    handleStroke(message.substring(7).trim());
                } else if (message.equals("READY")) {
                    // Player ready for new game (optional)
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
        if (currentRound == null) return;

        if (player.equals(currentRound.getDrawer())) {
            sendMessage("❌ You are the drawer! You cannot guess.");
            return;
        }

        server.broadcast("💭 " + player.getUsername() + " guessed: " + guess);

        boolean correct = gameState.checkGuess(guess);
        if (correct) {
            // Award points: guesser gets 100, drawer gets 50
            player.addScore(100);
            currentRound.getDrawer().addScore(50);
            
            server.broadcast("✅ " + player.getUsername() + " guessed correctly! (+100 points)");
            server.broadcast("✏️ " + currentRound.getDrawer().getUsername() + " gets +50 points!");

            // Send score updates
            sendMessage("SCORE_UPDATE:" + player.getScore());
            server.sendScoreUpdate(currentRound.getDrawer());

            // Check if game is over (5 rounds completed)
            if (gameState.getRoundNumber() > 5) {
                endGame();
                return;
            }

            // Start new round
            gameState.startNextRound();
            Round newRound = gameState.getCurrentRound();
            broadcastRoundStart(newRound);
        } else {
            sendMessage("❌ Wrong guess!");
        }
    }

    private void handleStroke(String strokeData) {
        Round currentRound = gameState.getCurrentRound();
        if (currentRound == null) return;

        if (!player.equals(currentRound.getDrawer())) {
            return;
        }

        currentRound.addDrawingStroke(strokeData);

        // Broadcast to everyone except drawer
        for (PlayerHandler client : server.getClients()) {
            if (!client.equals(this)) {
                client.sendMessage("STROKE:" + strokeData);
            }
        }
    }

    private void broadcastRoundStart(Round round) {
        // Human-readable message
        server.broadcast("🎮 Round " + round.getRoundNumber() + " started!");
        server.broadcast("✏️ Drawer: " + round.getDrawer().getUsername());
        
        // Structured messages for each client
        for (PlayerHandler client : server.getClients()) {
            client.sendRoundInfo(round);
        }
    }

    private void sendRoundInfo(Round round) {
        // Send word only if this player is the drawer
        String word = player.equals(round.getDrawer()) ? round.getWordToGuess() : "";
        String msg = "ROUND_STARTED:" + round.getRoundNumber() + "," + 
                     round.getDrawer().getUsername() + "," + word;
        sendMessage(msg);
    }

    private void endGame() {
        // Build leaderboard
        StringBuilder leaderboard = new StringBuilder("GAME_OVER:");
        var players = gameState.getPlayersSortedByScore();
        
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            leaderboard.append(p.getUsername()).append(":").append(p.getScore());
            if (i < players.size() - 1) {
                leaderboard.append(",");
            }
        }

        server.broadcast(leaderboard.toString());
        System.out.println("🏁 Game Over! Final scores sent.");
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