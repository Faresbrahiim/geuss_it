package client.ui;

import client.network.GameClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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
    private Label roundLabel, roleLabel;

    private GameClient client;
    private boolean isDrawer = false;
    private GraphicsContext gc;

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

        // Mouse handlers
        drawingCanvas.setOnMousePressed(event -> {
            System.out.println("🖱️ Mouse pressed at: " + event.getX() + ", " + event.getY());
            System.out.println("Is drawer: " + isDrawer + " | Username: " + (client != null ? client.getUsername() : "null"));
            
            if (isDrawer && client != null) {
                lastX = event.getX();
                lastY = event.getY();
                gc.fillOval(lastX - 2.5, lastY - 2.5, 5, 5);
            }
        });

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

        drawingCanvas.setOnMouseReleased(event -> {
            if (isDrawer) {
                lastX = -1;
                lastY = -1;
            }
        });

        sendGuessButton.setOnAction(e -> {
            String guess = guessField.getText().trim();
            if (!guess.isEmpty() && client != null && !isDrawer) {
                client.send("GUESS:" + guess);
                guessField.clear();
            }
        });

        guessField.setOnAction(e -> sendGuessButton.fire());
    }

    public void setClient(GameClient client) {
        this.client = client;
        client.setMessageCallback(this::handleServerMessage);
        System.out.println("✅ Client set in GameController | Username: " + client.getUsername());
    }

    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            System.out.println("📨 Received: " + message);

            // Handle STROKE
            if (message.startsWith("STROKE:")) {
                try {
                    String[] parts = message.substring(7).split(",");
                    double x1 = Double.parseDouble(parts[0]);
                    double y1 = Double.parseDouble(parts[1]);
                    double x2 = Double.parseDouble(parts[2]);
                    double y2 = Double.parseDouble(parts[3]);

                    gc.strokeLine(x1, y1, x2, y2);
                    System.out.println("✏️ Drew line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
                } catch (Exception e) {
                    System.err.println("❌ Error parsing stroke: " + message);
                    e.printStackTrace();
                }
                return;
            }

            // Handle ROUND_STARTED
            if (message.startsWith("ROUND_STARTED:")) {
                try {
                    String[] parts = message.substring(14).split(",");
                    int roundNumber = Integer.parseInt(parts[0]);
                    String drawer = parts[1].trim();
                    String myUsername = client.getUsername().trim();

                    isDrawer = drawer.equalsIgnoreCase(myUsername);

                    roundLabel.setText("Round " + roundNumber + " | Drawer: " + drawer);
                    roleLabel.setText(isDrawer ? "Your role: Drawer ✏️" : "Your role: Guesser 🤔");

                    System.out.println("🎮 Round " + roundNumber + " started!");
                    System.out.println("Drawer: " + drawer + " | My username: " + myUsername);
                    System.out.println("Am I drawer? " + isDrawer);

                    // Clear canvas
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                    gc.setFill(Color.BLACK);
                    
                    guessListView.getItems().clear();

                    guessField.setDisable(isDrawer);
                    sendGuessButton.setDisable(isDrawer);
                } catch (Exception e) {
                    System.err.println("❌ Error parsing round start: " + message);
                    e.printStackTrace();
                }
                return;
            }

            // Normal message
            guessListView.getItems().add(message);
        });
    }
}
