package com.example.Controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import java.util.Optional;
import java.io.IOException;
import com.example.Service.routeToController;

public class HomeController {

    private routeToController routeToController = new routeToController();
    private String username = "user1";


    @FXML
    private Button newDocButton;

    @FXML
    private Button browseButton;

    @FXML
    private Button joinButton;

    @FXML
    private TextField sessionCodeField;


    @FXML
    private TextField docNameField;


    private static final String VIEWER_CODE = "#yq1xrx";
    private static final String EDITOR_CODE = "#1Je02K";

    @FXML
    private Dialog<ButtonType> newDocDialog; // Change type from String to ButtonType
    
       @FXML
    private void handleNewDoc() throws IOException {
        // Create the custom dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Document");
        dialog.setHeaderText("Enter document name:");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the text field
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField documentName = new TextField();
        documentName.setPromptText("Document name");
        grid.add(new Label("Name:"), 0, 0);
        grid.add(documentName, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to string when create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return documentName.getText();
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    routeToController.createNewDocument(name,username);
                    loadEditorPage(false);
                } catch (IOException e) {
                    showError("Error creating document", e.getMessage());
                }
            } else {
                showError("Invalid Input", "Please enter a document name.");
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBrowse() {
        // Placeholder for Browse functionality
        System.out.println("Browse button clicked");
    }

    @FXML
    private void handleJoin() throws IOException {
        String sessionCode = sessionCodeField.getText().trim();
        if (sessionCode.equals(VIEWER_CODE)) {
            loadEditorPage(true); // Viewer code -> read-only mode
        } else if (sessionCode.equals(EDITOR_CODE)) {
            loadEditorPage(false); // Editor code -> editable mode
        } else {
            System.out.println("Invalid session code: " + sessionCode);
        }
    }

    private void loadEditorPage(boolean readOnly) throws IOException {
        // Load the editor page (editor.fxml)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/editor.fxml"));
        Scene editorScene = new Scene(fxmlLoader.load(), 800, 500);

        // Get the PrimaryController and set read-only mode
        PrimaryController controller = fxmlLoader.getController();
        controller.setReadOnly(readOnly);
        setupTextChangeListener(controller);

        // Get the current stage (window) and set the new scene
        Stage stage = (Stage) newDocButton.getScene().getWindow();
        stage.setScene(editorScene);
        stage.setTitle("Text Editor" + (readOnly ? " (Read-Only)" : ""));
        stage.show();
    }

    private void setupTextChangeListener(PrimaryController controller) {
        // Add a listener to the textProperty exposed by PrimaryController
        controller.textProperty().addListener((observable, oldValue, newValue) -> {
            // Only print if a character was added (length increased by 1)
            if (newValue.length() > oldValue.length()) {
                int[] rowCol = controller.getCursorPositionRowColumn();
                System.out.println("Character added. Cursor position - Row: " + rowCol[0] + ", Col: " + rowCol[1]);
            }
        });
    }

    // Mouse event handlers for New Doc button
    @FXML
    private void onNewDocMouseEntered() {
        newDocButton.setStyle("-fx-background-color: #03a9f4; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onNewDocMouseExited() {
        newDocButton.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    // Mouse event handlers for Browse button
    @FXML
    private void onBrowseMouseEntered() {
        browseButton.setStyle("-fx-background-color: #607d8b; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onBrowseMouseExited() {
        browseButton.setStyle("-fx-background-color: #455a64; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    // Mouse event handlers for Join button
    @FXML
    private void onJoinMouseEntered() {
        joinButton.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onJoinMouseExited() {
        joinButton.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }
}

