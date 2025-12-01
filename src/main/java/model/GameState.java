package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameState {

    private List<Player> players;
    private List<String> wordList;
    private Round currentRound;
    private int roundNumber;
    private Random random;
    private GameStateListener listener;

    public GameState() {
        this.players = new ArrayList<>();
        this.wordList = new ArrayList<>(Arrays.asList(
                "apple", "car", "house", "tree", "dog", "cat", "sun", "moon",
                "pizza", "guitar", "phone", "book", "flower", "clock", "plane"
        ));
        this.roundNumber = 1;
        this.random = new Random();
    }

    public void setListener(GameStateListener listener) {
        this.listener = listener;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getPlayersSortedByScore() {
        return players.stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .collect(Collectors.toList());
    }

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

        System.out.println("🎮 New Round: " + currentRound);

        if (listener != null) {
            listener.onRoundStarted(currentRound);
        }
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public boolean checkGuess(String guess) {
        if (currentRound == null) return false;
        return currentRound.getWordToGuess().equalsIgnoreCase(guess.trim());
    }

    public void notifyGuessResult(Player player, boolean correct) {
        if (listener != null) {
            listener.onGuessResult(player, correct);
        }
    }

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