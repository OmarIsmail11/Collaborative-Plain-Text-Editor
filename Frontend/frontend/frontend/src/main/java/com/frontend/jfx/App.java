package com.frontend.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextArea; // Add this import

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file
        URL fxmlLocation = App.class.getResource("/editor.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Cannot find editor.fxml at /editor.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        stage.setTitle("Text Editor");
        stage.setScene(scene);

        // Get the controller instance
        PrimaryController controller = fxmlLoader.getController();
        setupTextChangeListener(controller);

        stage.show();
    }

    private void setupTextChangeListener(PrimaryController controller) {
        // Add a listener to the TextArea's textProperty
        controller.getTextEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            // Only print if a character was added (length increased by 1)
            if (newValue.length() > oldValue.length()) {
                int[] rowCol = controller.getCursorPositionRowColumn();
                System.out.println("Character added. Cursor position - Row: " + rowCol[0] + ", Col: " + rowCol[1]);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Getter for TextArea
    public TextArea getTextEditor() {
        return null; // This will be accessed via the controller
    }
}