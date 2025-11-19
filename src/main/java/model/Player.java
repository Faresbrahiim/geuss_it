package model;

// player model .....
public class Player {
    // player has username  and score
    private String username;
    private int score;

    // Constructor
    public Player(String username) {
        this.username = username;
        this.score = 0; // initial score
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    // Add points
    public void addScore(int points) {
        this.score += points;
    }

    // Optional: Reset score
    public void resetScore() {
        this.score = 0;
    }

    // we add make -> override  to  change the implementation of toString  function just to print it with our format
    @Override
    public String toString() {
        return username + " (Score: " + score + ")";
    }
}
