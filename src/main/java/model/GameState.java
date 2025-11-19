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

    // Constructor
    public GameState() {
        this.players = new ArrayList<>();
        // arrays as an array into a list
        this.wordList = new ArrayList<>(Arrays.asList(
                "apple", "car", "house", "tree", "dog", "cat", "sun", "moon"
        ));
        // default round number is 1
        this.roundNumber = 1;
        // create an object to generate a randnom number
        this.random = new Random();
    }

    // Add a new player  into the list
    public void addPlayer(Player player) {
        players.add(player);
    }

    // Remove a player from the list
    public void removePlayer(Player player) {
        players.remove(player);
    }

    // Choose a random word for the round
    // chose a random int index from word list
    private String chooseRandomWord() {
        return wordList.get(random.nextInt(wordList.size()));
    }

    // Choose next drawer (simple rotation)  so all player can plaayyyyyy
    private Player chooseDrawer() {
        if (players.isEmpty()) return null;
        // Rotate based on round number
        return players.get((roundNumber - 1) % players.size());
    }

    // Start a new round
    public void startNextRound() {
        // chose drawer player
        Player drawer = chooseDrawer();
        // choose random word
        String word = chooseRandomWord();
        //  new round started
        currentRound = new Round(roundNumber, drawer, word);
        // increament the round
        roundNumber++;
        System.out.println("New Round Started: " + currentRound);
    }

    // Check if a guess is correct
    public boolean checkGuess(String guess) {
        if (currentRound == null) return false;
        // returns  true of flase
        return currentRound.getWordToGuess().equalsIgnoreCase(guess.trim());
    }

    // Getters
    public Round getCurrentRound() {
        return currentRound;
    }
    // get players.....
    public List<Player> getPlayers() {
        return players;
    }
}
