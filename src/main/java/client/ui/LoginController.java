package client.ui;

import java.io.IOException;

import client.network.GameClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Button connectButton;

    private GameClient client;

    @FXML
    public void initialize() {
        connectButton.setOnAction(e -> connectToServer());
    }

    private void connectToServer() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            new Alert(AlertType.WARNING, "Please enter a username!").showAndWait();
            return;
        }

        // ✅ Set username FIRST before connecting
        client = new GameClient("localhost", 5000, null);
        client.setUsername(username);
        
        System.out.println("✅ Connecting with username: " + username);

        if (!client.connect()) {
            new Alert(AlertType.ERROR, "Failed to connect to server!").showAndWait();
            return;
        }

        // Load game screen FIRST, then send username
        loadGameScreen();
        
        // ✅ Small delay to ensure GameController is ready
        new Thread(() -> {
            try {
                Thread.sleep(100);
                client.send(username);
                System.out.println("✅ Username sent to server: " + username);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadGameScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameScreen.fxml"));
            Parent root = loader.load();

            // Pass client to GameController
            GameController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) connectButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Guess & Draw Game");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Failed to load Game Screen!").showAndWait();
        }
    }

    public GameClient getClient() {
        return client;
    }
}