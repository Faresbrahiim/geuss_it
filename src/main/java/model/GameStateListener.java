package model;

public interface GameStateListener {
    // Called when a new round starts
    void onRoundStarted(Round round);

    // Called when a guess is made; correct = true if guess is correct
    void onGuessResult(Player player, boolean correct);

    // Called when drawing data is updated
    void onDrawingUpdated(Round round);
}
