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

// runnable interface hv the run method that execute when thread start
// describe one connected player
public class PlayerHandler implements Runnable {
    // the current socket of player
    private Socket socket;
    // core logic of game
    private GameState gameState;
    // the server instace will hold
    private GameServer server;
    private Player player;

    // to read
    private BufferedReader in;
    // to send 
    private PrintWriter out;

    public PlayerHandler(Socket socket, GameState gameState, GameServer server) {
        this.socket = socket;
        this.gameState = gameState;
        this.server = server;

        try {
            // buffred reader (collect sum of bytes) + get input as byte + convrt the byte 
            // InputStreamReader(...) → converts bytes into characters using a character encoding
            // instances
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // read  name from sockettt
            String username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                return;
            }
            
            username = username.trim();
            player = new Player(username);
            // add player to players
            gameState.addPlayer(player);

            System.out.println(" Player connected: " + username);
            // broadcats  to players ...
            server.broadcast(username + " has joined the game!");

            // Start first round if needed
            // else just send the current round  
            if (gameState.getCurrentRound() == null) {
                gameState.startNextRound();
                Round currentRound = gameState.getCurrentRound();
                // send round info like drawer round number ect...
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

            // Send initial score + will be 0 at first
            sendMessage("SCORE_UPDATE:" + player.getScore());
            // we read it from the game contoller  input
            String message;
            while ((message = in.readLine()) != null) {
                // debuge
                System.out.println("Received from " + username + ": " + message);

                if (message.startsWith("GUESS:")) {
                    System.err.println(message + "**********************************");
                    handleGuess(message.substring(6).trim());
                } else if (message.startsWith("STROKE:")) {
                    handleStroke(message.substring(7).trim());
                } else if (message.equals("READY")) {
                    // Player ready for new game 
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleGuess(String guess) {
        // current round info
        Round currentRound = gameState.getCurrentRound();
        if (currentRound == null) return;

        if (player.equals(currentRound.getDrawer())) {
            sendMessage(" You are the drawer! You cannot guess.");
            return;
        }

        server.broadcast(" " + player.getUsername() + " guessed: " + guess);

        boolean correct = gameState.checkGuess(guess);
        if (correct) {
            player.addScore(100);
            currentRound.getDrawer().addScore(50);
            
            server.broadcast( player.getUsername() + " guessed correctly! (+100 points)");
            server.broadcast( currentRound.getDrawer().getUsername() + " gets +50 points!");

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
            sendMessage("Wrong guess!");
        }
    }

    private void handleStroke(String strokeData) {
        Round currentRound = gameState.getCurrentRound();
        if (currentRound == null) return;

        if (!player.equals(currentRound.getDrawer())) {
            return;
        }
        // to dave storke
        currentRound.addDrawingStroke(strokeData);

        // Broadcast to everyone except drawer
        for (PlayerHandler client : server.getClients()) {
            // execlude the drawer
            if (!client.equals(this)) {
                client.sendMessage("STROKE:" + strokeData);
            }
        }
    }

    // sends the round after stating the game (round)
    private void broadcastRoundStart(Round round) {
    
        server.broadcast(" Round " + round.getRoundNumber() + " started!");
        server.broadcast(" Drawer: " + round.getDrawer().getUsername());
        
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
    }

    private void disconnect() {
        try {
            if (player != null) {
                gameState.removePlayer(player);
                server.removeClient(this);
                server.broadcast(player.getUsername() + " disconnected.");
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // get the player
    public Player getPlayer() {
        return player;
    }
    // send the msg via socket
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}