package com.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.util.Optional;
import java.io.IOException;
import com.example.Service.routeToController;
import com.example.Model.Document;
import com.example.config.WebSocketConfig;

public class HomeController {

    private routeToController routeToController = new routeToController();

    @FXML
    private Button newDocButton;

    @FXML
    private Button browseButton;

    @FXML
    private Button joinButton;

    @FXML
    private TextField sessionCodeField;

    private static final String VIEWER_CODE = "#yq1xrx";
    private static final String EDITOR_CODE = "#1Je02K";

    @FXML
    private void handleNewDoc() throws IOException {
        // Load the FXML dialog
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/new_document_dialog.fxml"));
        // If FXML is in src/main/resources/fxml/, use: getClass().getResource("/fxml/new_document_dialog.fxml")
        DialogPane dialogPane = fxmlLoader.load();

        // Get the controller
        NewDocumentDialogController dialogController = fxmlLoader.getController();

        // Create the dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Document");
        dialog.setDialogPane(dialogPane);

        // Set the result converter
        dialog.setResultConverter(buttonType -> {
            dialogController.processResult(buttonType);
            return dialogController.getDocumentName();
        });

        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String username = dialogController.getUsername();
            if (!name.trim().isEmpty() && !username.trim().isEmpty()) {
                try {
                    try {
                        Document newDoc = routeToController.createNewDocument(name, username);

                            String editorCode = newDoc.getEditorCode(); // Use this for viewer/editor
                            String viewerCode = newDoc.getViewerCode();
                            String docName = newDoc.getDocName();
                            String content = newDoc.getDocText();
                            String docID    = newDoc.getDocID();
                            loadEditorPage(false,docName,username,docID);
                            if (newDoc == null)
                            System.err.println("Failed to create document");
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("Null fel doc hasalaha ana " + e.getMessage());
                    }
                    // to do zawed fel load editor page 3 args
                    //editor code w viewer code fel tab el 3al shemal
                    //content fel textArea


                } catch (IOException e) {
                    showError("Error creating document", e.getMessage());
                }
            } else {
                showError("Invalid Input", "Please enter both a document name and a username.");
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
        Document newDoc = new Document();
         newDoc = routeToController.getDocumentID(sessionCode);
        if (newDoc != null) {

        }else{
            showError("Document not found","Recheck Session Code");
            return;
        }

        // Prompt for username
        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Join Document");
        usernameDialog.setHeaderText("Enter your username:");
        usernameDialog.setContentText("Username:");
        Optional<String> usernameResult = usernameDialog.showAndWait();

        usernameResult.ifPresent(username -> {
            if (!username.trim().isEmpty()) {
                try {

                    if(sessionCode == newDoc.getEditorCode()){
                        loadEditorPage(true, newDoc.getDocName(), username,newDoc.getDocID(), newDoc.getViewerCode(), newDoc.getEditorCode());
                    }else{
                        loadEditorPage(false, newDoc.getDocName(), username,newDoc.getDocID(), newDoc.getViewerCode(), newDoc.getEditorCode());
                    }
                } catch (IOException e) {
                    showError("Error joining document", e.getMessage());
                }
            } else {
                showError("Invalid Input", "Please enter a username.");
            }
        });
    }

    private void loadEditorPage(boolean readOnly, String documentName, String username,String docID,String Viewer_Code,String Editor_Code) throws IOException {
        // Load the editor page (editor.fxml)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/editor.fxml"));
        Scene editorScene = new Scene(fxmlLoader.load(), 800, 500);

        // Get the PrimaryController and set properties
        PrimaryController controller = fxmlLoader.getController();
        controller.setReadOnly(readOnly);
        controller.InitializeDocContents(docID,documentName,username,Viewer_Code,Editor_Code);
        setupTextChangeListener(controller);

        // Get the current stage (window) and set the new scene
        Stage stage = (Stage) newDocButton.getScene().getWindow();
        stage.setScene(editorScene);
        stage.setTitle(documentName + (readOnly ? " (Read-Only)" : ""));
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