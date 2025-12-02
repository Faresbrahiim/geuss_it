package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// game state class -> describe the behaviouuur of the game
public class GameState {
    // the list of connected player
    private List<Player> players;
    // words to geuss
    private List<String> wordList;
    // the current round  information
    private Round currentRound;
    // round number
    private int roundNumber;
    // for random number
    private Random random;
    // Used to notify the UI or server when something happens
    private GameStateListener listener;
    // contrcutor
    public GameState() {
        this.players = new ArrayList<>(); // start  empty 
        this.wordList = new ArrayList<>(Arrays.asList( 
                "apple", "car", "house", "tree", "dog", "cat", "sun", "moon",
                "pizza", "guitar", "phone", "book", "flower", "clock", "plane"
        )); // the words to geuss
        this.roundNumber = 1; // round number
        this.random = new Random(); // instance of random
    }

    public void setListener(GameStateListener listener) {
        this.listener = listener;
    }

    // add player to players from list
    public void addPlayer(Player player) {
        players.add(player);
    }
    // remove the player from list
    public void removePlayer(Player player) {
        players.remove(player);
    }
    // get all player from list
    public List<Player> getPlayers() {
        return players;
    }

    //
    public List<Player> getPlayersSortedByScore() {
        return players.stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .collect(Collectors.toList());
    }
    // choose a random work in marge of wordlist size
    private String chooseRandomWord() {
        return wordList.get(random.nextInt(wordList.size()));
    }
    // choose drawer in rotation  from the players list it start by the first one...
    private Player chooseDrawer() {
        if (players.isEmpty()) return null;
        return players.get((roundNumber - 1) % players.size());
    }

    // method to start next round  (calls the other methods) + public
    public void startNextRound() {
        Player drawer = chooseDrawer();
        String word = chooseRandomWord();
        currentRound = new Round(roundNumber, drawer, word);
        roundNumber++;

        System.out.println(" New Round: " + currentRound);

        if (listener != null) {
            listener.onRoundStarted(currentRound);
        }
    }
    // get current round
    public Round getCurrentRound() {
        return currentRound;
    }
    // get round number
    public int getRoundNumber() {
        return roundNumber;
    }
    // check  the guess word with   the current round word to guess
    public boolean checkGuess(String guess) {
        if (currentRound == null) return false;
        return currentRound.getWordToGuess().equalsIgnoreCase(guess.trim());
    }

    //  These methods inform the listener when something happens
    // someONE guessed 
    public void notifyGuessResult(Player player, boolean correct) {
        if (listener != null) {
            listener.onGuessResult(player, correct);
        }
    }
    // notify new round started
    public void notifyRoundStarted(Round round) {
        if (listener != null) {
            listener.onRoundStarted(round);
        }
    }
    // notify drawind updated
    public void notifyDrawingUpdated(Round round) {
        if (listener != null) {
            listener.onDrawingUpdated(round);
        }
    }
}