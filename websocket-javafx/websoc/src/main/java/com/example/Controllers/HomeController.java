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

    @FXML
    private void handleNewDoc() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/new_document_dialog.fxml"));
        DialogPane dialogPane = fxmlLoader.load();

        NewDocumentDialogController dialogController = fxmlLoader.getController();

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Document");
        dialog.setDialogPane(dialogPane);

        dialog.setResultConverter(buttonType -> {
            dialogController.processResult(buttonType);
            return dialogController.getDocumentName();
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String username = dialogController.getUsername();
            if (!name.trim().isEmpty() && !username.trim().isEmpty()) {
                try {
                    Document newDoc = routeToController.createNewDocument(name, username);

                    if (newDoc == null) {
                        System.err.println("Failed to create document");
                        return;
                    }

                    loadEditorPage(false, newDoc.getDocName(), username, newDoc.getDocID(),
                            newDoc.getViewerCode(), newDoc.getEditorCode());
                } catch (IOException e) {
                    showError("Error creating document", e.getMessage());
                }
            } else {
                showError("Invalid Input", "Please enter both a document name and a username.");
            }
        });
    }

    @FXML
    private void handleBrowse() {
        System.out.println("Browse button clicked");
    }

    @FXML
    private void handleJoin() throws IOException {
        String sessionCode = sessionCodeField.getText().trim();
        Document doc = routeToController.getDocumentID(sessionCode);

        if (doc == null) {
            showError("Document not found", "Recheck Session Code");
            return;
        }

        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Join Document");
        usernameDialog.setHeaderText("Enter your username:");
        usernameDialog.setContentText("Username:");
        Optional<String> usernameResult = usernameDialog.showAndWait();

        usernameResult.ifPresent(username -> {
            if (!username.trim().isEmpty()) {
                try {
                    boolean isEditor = sessionCode.equals(doc.getEditorCode());
                    loadEditorPage(!isEditor, doc.getDocName(), username, doc.getDocID(),
                            doc.getViewerCode(), doc.getEditorCode());
                } catch (IOException e) {
                    showError("Error joining document", e.getMessage());
                }
            } else {
                showError("Invalid Input", "Please enter a username.");
            }
        });
    }

    private void loadEditorPage(boolean readOnly, String documentName, String username,
                                String docID, String viewerCode, String editorCode) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/editor.fxml"));
        Scene editorScene = new Scene(fxmlLoader.load(), 800, 500);

        PrimaryController controller = fxmlLoader.getController();
        controller.setReadOnly(readOnly);
        controller.InitializeDocContents(docID, documentName, username, viewerCode, editorCode);
        setupTextChangeListener(controller);

        Stage stage = (Stage) newDocButton.getScene().getWindow();
        stage.setScene(editorScene);
        stage.setTitle(documentName + (readOnly ? " (Read-Only)" : ""));
        stage.show();
    }

    private void setupTextChangeListener(PrimaryController controller) {
        controller.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > oldValue.length()) {
                int[] rowCol = controller.getCursorPositionRowColumn();
                System.out.println("Character added. Cursor position - Row: " + rowCol[0] + ", Col: " + rowCol[1]);
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

    // Style effects for buttons
    @FXML
    private void onNewDocMouseEntered() {
        newDocButton.setStyle("-fx-background-color: #03a9f4; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onNewDocMouseExited() {
        newDocButton.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onBrowseMouseEntered() {
        browseButton.setStyle("-fx-background-color: #607d8b; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onBrowseMouseExited() {
        browseButton.setStyle("-fx-background-color: #455a64; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onJoinMouseEntered() {
        joinButton.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }

    @FXML
    private void onJoinMouseExited() {
        joinButton.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
    }
}
