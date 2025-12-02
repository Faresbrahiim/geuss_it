package client.ui;

import client.network.GameClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class GameController {

    @FXML
    private Canvas drawingCanvas;

    @FXML
    private ListView<String> guessListView;

    @FXML
    private TextField guessField;

    @FXML
    private Button sendGuessButton;

    @FXML
    private Label roundLabel, roleLabel, wordLabel, scoreLabel;

    private GameClient client;
    private boolean isDrawer = false;
    private GraphicsContext gc;
    private int myScore = 0;

    private double lastX = -1;
    private double lastY = -1;

    @FXML
    public void initialize() {
        System.out.println("✅ GameController initialized!");
        
        gc = drawingCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);

        // Clear canvas with white background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        gc.setFill(Color.BLACK);

        // Initially hide word label
        if (wordLabel != null) {
            wordLabel.setVisible(false);
        }

        // Mouse handlers for drawing
        drawingCanvas.setOnMousePressed(event -> {
            if (isDrawer && client != null) {
                lastX = event.getX();
                lastY = event.getY();
                gc.fillOval(lastX - 2.5, lastY - 2.5, 5, 5);
            }
        });
        // drag
        drawingCanvas.setOnMouseDragged(event -> {
            if (isDrawer && client != null) {
                double x = event.getX();
                double y = event.getY();

                if (lastX >= 0 && lastY >= 0) {
                    gc.strokeLine(lastX, lastY, x, y);
                }

                String strokeData = lastX + "," + lastY + "," + x + "," + y;
                client.send("STROKE:" + strokeData);

                lastX = x;
                lastY = y;
            }
        });
        // reset
        drawingCanvas.setOnMouseReleased(event -> {
            if (isDrawer) {
                lastX = -1;
                lastY = -1;
            }
        });

        // Guess button handler
        sendGuessButton.setOnAction(e -> {
            String guess = guessField.getText().trim();
            if (!guess.isEmpty() && client != null && !isDrawer) {
                client.send("GUESS:" + guess);
                guessField.clear();
            }
        });

        guessField.setOnAction(e -> sendGuessButton.fire());

        // Initialize score label
        updateScoreLabel();
    }

    public void setClient(GameClient client) {
        this.client = client;
        client.setMessageCallback(this::handleServerMessage);
        System.out.println(" Client set in GameController | Username: " + client.getUsername());
    }

    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            System.out.println(" Received: " + message);

            // Handle STROKE to draw
            if (message.startsWith("STROKE:")) {
                try {
                    String[] parts = message.substring(7).split(",");
                    double x1 = Double.parseDouble(parts[0]);
                    double y1 = Double.parseDouble(parts[1]);
                    double x2 = Double.parseDouble(parts[2]);
                    double y2 = Double.parseDouble(parts[3]);
                    gc.strokeLine(x1, y1, x2, y2);
                } catch (Exception e) {
                    System.err.println(" Error parsing stroke: " + message);
                }
                return;
            }

            // Handle ROUND_STARTED
            if (message.startsWith("ROUND_STARTED:")) {
                try {
                    // Format: ROUND_STARTED:roundNum,drawer,word
                    String[] parts = message.substring(14).split(",", 3);
                    int roundNumber = Integer.parseInt(parts[0]);
                    String drawer = parts[1].trim();
                    String word = parts.length > 2 ? parts[2].trim() : "";
                    String myUsername = client.getUsername().trim();

                    isDrawer = drawer.equalsIgnoreCase(myUsername);

                    roundLabel.setText("Round " + roundNumber);
                    roleLabel.setText(isDrawer ? "You are drawing! ✏️" : "Guess the word! 🤔");

                    // Show word only to drawer
                    if (isDrawer && wordLabel != null) {
                        wordLabel.setText("Draw: " + word);
                        wordLabel.setVisible(true);
                        wordLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
                    } else if (wordLabel != null) {
                        wordLabel.setVisible(false);
                    }

                    // Clear canvas
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                    gc.setFill(Color.BLACK);
                    
                    guessListView.getItems().clear();
                    guessListView.getItems().add("🎮 Round " + roundNumber + " started!");
                    guessListView.getItems().add("Drawer: " + drawer);

                    guessField.setDisable(isDrawer);
                    sendGuessButton.setDisable(isDrawer);
                } catch (Exception e) {
                    System.err.println("❌ Error parsing round start: " + message);
                    e.printStackTrace();
                }
                return;
            }

            // Handle SCORE_UPDATE
            if (message.startsWith("SCORE_UPDATE:")) {
                try {
                    // Format: SCORE_UPDATE:score
                    int score = Integer.parseInt(message.substring(13).trim());
                    myScore = score;
                    updateScoreLabel();
                } catch (Exception e) {
                    System.err.println("❌ Error parsing score: " + message);
                }
                return;
            }

            // Handle GAME_OVER (leaderboard)
            if (message.startsWith("GAME_OVER:")) {
                try {
                    // Format: GAME_OVER:username1:score1,username2:score2,...
                    String leaderboardData = message.substring(10);
                    showLeaderboard(leaderboardData);
                } catch (Exception e) {
                    System.err.println("❌ Error parsing leaderboard: " + message);
                }
                return;
            }

            // Normal message
            guessListView.getItems().add(message);
        });
    }

    private void updateScoreLabel() {
        if (scoreLabel != null) {
            scoreLabel.setText("Your Score: " + myScore);
        }
    }

    private void showLeaderboard(String data) {
        // Parse leaderboard data
        String[] entries = data.split(",");
        StringBuilder leaderboard = new StringBuilder(" GAME OVER - LEADERBOARD 🏆\n\n");
        
        for (int i = 0; i < entries.length; i++) {
            String[] parts = entries[i].split(":");
            if (parts.length == 2) {
                String username = parts[0];
                String score = parts[1];
                String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "  ";
                leaderboard.append(medal).append(" ")
                           .append(i + 1).append(". ")
                           .append(username).append(": ")
                           .append(score).append(" points\n");
            }
        }

        // Show in alert dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over!");
        alert.setHeaderText("Final Results");
        alert.setContentText(leaderboard.toString());
        
        ButtonType playAgainBtn = new ButtonType("Play Again");
        ButtonType exitBtn = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(playAgainBtn, exitBtn);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == playAgainBtn) {
                // Reset game
                myScore = 0;
                updateScoreLabel();
                guessListView.getItems().clear();
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                gc.setFill(Color.BLACK);
                client.send("READY"); // Signal ready for new game
            } else {
                // Exit
                client.disconnect();
                System.exit(0);
            }
        });
    }
}
