package model;

import java.util.ArrayList;
import java.util.List;

///  round class
public class Round {
    // rounds like  round 1  , round 2  , round 3
    private int roundNumber;
    // player which it the one who will draw
    private Player drawer;       // current drawer
    // word to geuss -> it will be randomly
    private String wordToGuess;  // word for this round
    // store coordinations like  and color ect..
    private List<String> drawingData; // placeholder for strokes

    // constructor .... , will have round number  , and  the drawer + word to geuss
    public Round(int roundNumber, Player drawer, String wordToGuess) {
        this.roundNumber = roundNumber;
        this.drawer = drawer;
        this.wordToGuess = wordToGuess;
        // stork is  like and log of data can be coordinates ui or anything
        this.drawingData = new ArrayList<>();
    }

    // Getters
    public int getRoundNumber() {
        return roundNumber;
    }

    public Player getDrawer() {
        return drawer;
    }

    public String getWordToGuess() {
        return wordToGuess;
    }

    public List<String> getDrawingData() {
        return drawingData;
    }

    // Add a drawing stroke (simplified as a string for now)
    public void addDrawingStroke(String stroke) {
        drawingData.add(stroke);
    }

    // Clear drawing (for new round or reset)
    public void clearDrawing() {
        drawingData.clear();
    }

    @Override
    public String toString() {
        return "Round " + roundNumber + " | Drawer: " + drawer.getUsername() + " | Word: " + wordToGuess;
    }
}
