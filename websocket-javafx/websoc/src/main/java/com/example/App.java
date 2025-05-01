package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import java.net.URL;

import java.io.IOException;

/// JavaFX App
public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the homepage (home.fxml)
        URL fxmlLocation = App.class.getResource("/home.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find home.fxml at /home.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);
        stage.setTitle("TextSync - Home");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public TextArea getTextEditor() {
        return null;
    }
}