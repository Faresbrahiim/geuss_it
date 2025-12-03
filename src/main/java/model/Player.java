package model;

// player model ..... describe the player 
public class Player {
    // player has username  and score 
    private String username;
    private int score;

    // Constructor
    public Player(String username) {
        this.username = username;
        this.score = 0; // initial score with zero
    }

    // Get username
    public String getUsername() {
        return username;
    }
    // get score
    public int getScore() {
        return score;
    }

    // Add points to score 
    public void addScore(int points) {
        this.score += points;
    }


    // we add make -> override  to  change the implementation of toString  function just to print it with our format
    @Override
    public String toString() {
        return username + " (Score: " + score + ")";
    }
}
