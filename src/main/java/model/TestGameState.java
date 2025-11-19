package model;

public class TestGameState {
    public static void main(String[] args) {
        // Create GameState
        GameState game = new GameState();

        // Add players
        game.addPlayer(new Player("Alice"));
        game.addPlayer(new Player("Bob"));

        // Start first round
        game.startNextRound();

        // Print round info
        System.out.println("Drawer: " + game.getCurrentRound().getDrawer().getUsername());
        System.out.println("Word to guess: " + game.getCurrentRound().getWordToGuess());

        // Test a guess
        String guess = "apple";
        if (game.checkGuess(guess)) {
            System.out.println("Correct guess!");
        } else {
            System.out.println("Wrong guess!");
        }

        // Start next round
        game.startNextRound();
        System.out.println("Next round drawer: " + game.getCurrentRound().getDrawer().getUsername());
    }
}
