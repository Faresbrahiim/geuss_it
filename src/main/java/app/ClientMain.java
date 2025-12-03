package app;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Application is the JavaFX base class for GUI apps.
public class ClientMain extends Application {
    // start(Stage) is called on the JavaFX Application Thread.
    // which mean even the ui or javaFx is running on another thread to not freeze ui
    // stage is a window
    @Override
    public void start(Stage stage) throws Exception {
        // load ui layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScreen.fxml"));
        // When this executes:
        //JavaFX opens the FXML file.
        //Reads all its XML structure.
        //Creates the UI nodes (TextField, Button, Pane…).
        //Creates an instance of your controller LoginController.
        //Injects all @FXML fields inside that controller.
        //Calls the controller’s initialize() method.
        Scene scene = new Scene(loader.load());
        // scen ui content
        stage.setScene(scene);
        stage.setTitle("Guess Draw");
        stage.show();
    }
    // launch() is a static helper that
    public static void main(String[] args) {
        launch();
    }
}
