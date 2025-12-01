package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameState {

    private List<Player> players;        // all players in the game
    private List<String> wordList;       // list of words to choose from
    private Round currentRound;          // the current round
    private int roundNumber;             // current round number
    private Random random;

    // Listener for UI updates
    private GameStateListener listener;

    // Constructor
    public GameState() {
        this.players = new ArrayList<>();
        this.wordList = new ArrayList<>(Arrays.asList(
                "apple", "car", "house", "tree", "dog", "cat", "sun", "moon"
        ));
        this.roundNumber = 1;
        this.random = new Random();
    }

    // --- Listener ---
    public void setListener(GameStateListener listener) {
        this.listener = listener;
    }

    // --- Player management ---
    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    // --- Round management ---
    private String chooseRandomWord() {
        return wordList.get(random.nextInt(wordList.size()));
    }

    private Player chooseDrawer() {
        if (players.isEmpty()) return null;
        return players.get((roundNumber - 1) % players.size());
    }

    public void startNextRound() {
        Player drawer = chooseDrawer();
        String word = chooseRandomWord();
        currentRound = new Round(roundNumber, drawer, word);
        roundNumber++;

        System.out.println("New Round Started: " + currentRound);

        // Notify UI or listener
        if (listener != null) {
            listener.onRoundStarted(currentRound);
        }
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    // --- Guessing ---
    public boolean checkGuess(String guess) {
        if (currentRound == null) return false;
        boolean correct = currentRound.getWordToGuess().equalsIgnoreCase(guess.trim());
        return correct;
    }

    // Call this from PlayerHandler to notify about a guess result
    public void notifyGuessResult(Player player, boolean correct) {
        if (listener != null) {
            listener.onGuessResult(player, correct);
        }
    }

    // --- Drawing ---
    public void addDrawingStroke(String stroke) {
        if (currentRound != null) {
            currentRound.addDrawingStroke(stroke);
            if (listener != null) {
                listener.onDrawingUpdated(currentRound);
            }
        }
    }

    // Notify listeners manually (optional)
    public void notifyRoundStarted(Round round) {
        if (listener != null) {
            listener.onRoundStarted(round);
        }
    }

    public void notifyDrawingUpdated(Round round) {
        if (listener != null) {
            listener.onDrawingUpdated(round);
        }
    }
}
