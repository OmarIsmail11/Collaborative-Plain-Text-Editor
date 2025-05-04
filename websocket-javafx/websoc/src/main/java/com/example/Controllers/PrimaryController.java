package com.example.Controllers;

import com.example.Model.CRDTNode;
import com.example.Model.CRDTTree;
import com.example.Model.Operation;
import com.example.Model.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.example.config.WebSocketConfig;
import com.example.Service.routeToController;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Controller;
import java.util.concurrent.ExecutorService;

import javax.swing.*;

@Controller
public class PrimaryController {

    @FXML
    private TextArea textEditor;

    @FXML
    private Label viewerCodeLabel;

    @FXML
    private Label editorCodeLabel;

    @FXML
    private Button copyViewerButton;

    @FXML
    private Button copyEditorButton;

    @FXML
    private Button undoButton;

    @FXML
    private Button redoButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button addCommentButton;

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label currentUserLabel;

    public void setCurrentUserLabel(Label currentUserLabel) {
        this.currentUserLabel.setText("hussein");
    }

    @FXML
    private Label crabUserLabel;

    @FXML
    private Label foxUserLabel;

    @FXML
    private Label penguinUserLabel;

    String sessionCode;
    private boolean isReadOnly = false;
    private StringProperty textProperty = new SimpleStringProperty("");
    private String documentName;
    private List<Operation> operationBuffer = new ArrayList<>();
    private routeToController routeToController = new routeToController();

    private String DocID;
    public CRDTTree  crdtTree = new CRDTTree();
    public User currentUser;
    public WebSocketConfig webSocketClient = new WebSocketConfig();
    private boolean isUpdating = false;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Object crdtLock = new Object();


    @FXML
    private void initialize() {
        // Set read-only mode
        textEditor.setEditable(!isReadOnly);

        // Request focus on TextArea after initialization
        textEditor.requestFocus();

        // Initialize available user labels
        availableUserLabels = new ArrayList<>();
        if (crabUserLabel != null) availableUserLabels.add(crabUserLabel);
        if (foxUserLabel != null) availableUserLabels.add(foxUserLabel);
        if (penguinUserLabel != null) availableUserLabels.add(penguinUserLabel);

        // Add key event handlers for real-time character insertion tracking
        textEditor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        textEditor.addEventHandler(KeyEvent.KEY_TYPED, this::handleKeyTyped);




        // Debug: Print initial state and check if UI elements are properly initialized
        System.out.println("Editor initialized. Read-only: " + isReadOnly);
        printInitializationStatus();

    }

    // Debug method to check if UI elements are properly initialized
    private void printInitializationStatus() {
        System.out.println("textEditor: " + (textEditor != null ? "OK" : "NULL"));
        System.out.println("commentsContainer: " + (commentsContainer != null ? "OK" : "NULL"));
        System.out.println("currentUserLabel: " + (currentUserLabel != null ? "OK" : "NULL"));
        System.out.println("crabUserLabel: " + (crabUserLabel != null ? "OK" : "NULL"));
        System.out.println("foxUserLabel: " + (foxUserLabel != null ? "OK" : "NULL"));
        System.out.println("penguinUserLabel: " + (penguinUserLabel != null ? "OK" : "NULL"));
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.BACK_SPACE) {
            int caretPos = textEditor.getCaretPosition();
            if (caretPos > 0 && caretPos <= crdtTree.visibleNodes.size()) {
                int deletePos = caretPos - 1;
                CRDTNode delNode;
                synchronized (crdtLock) { // Synchronize CRDT access
                    delNode = crdtTree.visibleNodes.get(deletePos);
                    crdtTree.delete(deletePos, currentUser.getUserID());
                    currentUser.addToUndoStack("delete", delNode, deletePos);
                }
                Operation operation = new Operation("delete", delNode, deletePos);

                executorService.submit(() -> {
                    try {
                        webSocketClient.sendOperation(sessionCode, operation);
                        System.out.println("Sent operation: delete at position: " + deletePos);
                    } catch (Exception e) {
                        System.err.println("Failed to send operation: " + e.getMessage());
                    }
                });

                System.out.println("Backspace pressed - Deleted at position: " + deletePos);
                crdtTree.printCRDTTree();
                crdtTree.printText();

                int newCaretPos = deletePos;
                updateTextEditorContent(newCaretPos);
                event.consume();
            }
        } else if (event.getCode() == KeyCode.DELETE) {
            int caretPos = textEditor.getCaretPosition();
            if (caretPos < crdtTree.visibleNodes.size()) {
                CRDTNode delNode;
                synchronized (crdtLock) { // Synchronize CRDT access
                    delNode = crdtTree.visibleNodes.get(caretPos);
                    crdtTree.delete(caretPos, currentUser.getUserID());
                    currentUser.addToUndoStack("delete", delNode, caretPos);
                    delNode.setUserID(currentUser.getUserID());
                }
                Operation operation = new Operation("delete", delNode, caretPos);

                executorService.submit(() -> {
                    try {
                        webSocketClient.sendOperation(sessionCode, operation);
                        System.out.println("Sent operation: delete at position: " + caretPos);
                    } catch (Exception e) {
                        System.err.println("Failed to send operation: " + e.getMessage());
                    }
                });

                System.out.println("Delete pressed - Deleted at position: " + caretPos);
                crdtTree.printCRDTTree();
                crdtTree.printText();

                int newCaretPos = caretPos;
                updateTextEditorContent(newCaretPos);
                event.consume();
            }
        }
    }

    private void handleKeyTyped(KeyEvent event) {
        String character = event.getCharacter();
        if (character.length() == 0 || character.codePointAt(0) < 32 ||
                character.equals("\b") || character.equals("\u007F")) {
            return;
        }

        int caretPos = textEditor.getCaretPosition();
        for (int i = 0; i < character.length(); i++) {
            char ch = character.charAt(i);
            CRDTNode newNode = new CRDTNode(
                    currentUser.getUserID() + "_" + LocalDateTime.now().toString(),
                    ch,
                    LocalDateTime.now().toString(),
                    false,
                    null,
                    currentUser.getUserID(),
                    caretPos + i
            );
            CRDTNode node;
            synchronized (crdtLock) { // Synchronize CRDT access
                node = crdtTree.insert(newNode);
                currentUser.addToUndoStack("insert", node, caretPos + i);
            }
            Operation operation = new Operation("insert", node, caretPos + i);

            executorService.submit(() -> {
                try {
                    webSocketClient.sendOperation(sessionCode, operation);
                    System.out.println("Sent operation: " + operation.getType() + " for node: " + node.getId());
                } catch (Exception e) {
                    System.err.println("Failed to send operation: " + e.getMessage());
                }
            });

            System.out.println("Character typed: '" + ch + "' at position: " + (caretPos + i));
            crdtTree.printCRDTTree();
            crdtTree.printText();
        }

        int newCaretPos = caretPos + character.length();
        updateTextEditorContent(newCaretPos);
        event.consume();
    }

    // Method to update the text editor content based on CRDT state with specific caret position
    private void updateTextEditorContent(int newCaretPos) {
        isUpdating = true;
        try {
            StringBuilder content = new StringBuilder();
            for (CRDTNode node : crdtTree.visibleNodes) {
                content.append(node.getValue());
            }

            // Make sure caret position is within bounds
            int safeCaretPos = Math.min(Math.max(newCaretPos, 0), content.length());

            // Update text - using Platform.runLater to ensure it's on the JavaFX thread
            Platform.runLater(() -> {
                // Update text
                textEditor.setText(content.toString());

                // Set the specified caret position
                textEditor.positionCaret(safeCaretPos);

                System.out.println("UI updated. New content length: " + content.length() + ", Caret at: " + safeCaretPos);
            });
        } finally {
            isUpdating = false;
        }
    }

    // Overloaded method that uses current caret position (for backward compatibility)
    private void updateTextEditorContent() {
        int caretPos = textEditor.getCaretPosition();
        updateTextEditorContent(caretPos);
    }

    // Method to handle remote operations received from WebSocket
    public void handleRemoteOperation(Operation operation) {
        // Process CRDT operation in background thread
        executorService.submit(() -> {
            try {
                synchronized (crdtLock) { // Synchronize access to crdtTree
                    System.out.println("Processing operation from server: " + operation.getType());
                    if ("insert".equals(operation.getType())) {
                        CRDTNode node = operation.getNode();
                        int position = operation.getIndex();
                        crdtTree.insert(node);
                    } else if ("delete".equals(operation.getType())) {
                        int position = operation.getIndex();
                        crdtTree.delete(position, operation.getNode().getUserID());
                        crdtTree.printCRDTTree();
                    }
                }

                // Update UI on FX thread
                Platform.runLater(() -> {
                    isUpdating = true;
                    updateTextEditorContent();
                    isUpdating = false;
                    System.out.println("UI updated after remote operation: " + operation.getType());
                });
            } catch (Exception e) {
                System.err.println("Error processing remote operation: " + e.getMessage());
            }
        });
    }


    @FXML
    private void copyViewerCode() {
        if (viewerCodeLabel == null) {
            System.err.println("viewerCodeLabel is null in copyViewerCode()");
            return;
        }

        String code = viewerCodeLabel.getText();
        copyToClipboard(code);
        System.out.println("Copied Viewer Code to clipboard: " + code);
    }

    @FXML
    private void copyEditorCode() {
        if (editorCodeLabel == null) {
            System.err.println("editorCodeLabel is null in copyEditorCode()");
            return;
        }

        String code = editorCodeLabel.getText();
        copyToClipboard(code);
        System.out.println("Copied Editor Code to clipboard: " + code);
    }

    private void copyToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    @FXML
    private void undoAction() {
        if (textEditor == null) {
            System.err.println("TextEditor is null in undoAction()");
            return;
        }

        textEditor.undo();
        System.out.println("Undo action triggered");
    }

    @FXML
    private void redoAction() {
        if (textEditor == null) {
            System.err.println("TextEditor is null in redoAction()");
            return;
        }

        textEditor.redo();
        System.out.println("Redo action triggered");
    }

    @FXML
    private void saveAction() {
        System.out.println("Save action triggered");
        String text = textEditor.getText();
        System.out.println("THIS IS TEXT: " + text);
        routeToController.saveDocument(this.DocID,text);

    }

    @FXML
    private void loadAction() {
        // Open a file chooser dialog to select a text file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                // Read the file content as a string
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

                // Set the content of the textEditor (TextArea) to the file's content
                textEditor.setText(content);

                System.out.println("File loaded successfully.");
            } catch (IOException e) {
                // Show an error message if the file couldn't be read
                showError("Error", "Failed to load the file: " + e.getMessage());
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    public int getCursorPosition() {
        if (textEditor == null) {
            System.err.println("TextEditor is null in getCursorPosition()");
            return 0;
        }

        return textEditor.getCaretPosition();
    }

    public int[] getCursorPositionRowColumn() {
        if (textEditor == null) {
            System.err.println("TextEditor is null in getCursorPositionRowColumn()");
            return new int[] {1, 1};
        }

        int caretPos = textEditor.getCaretPosition();
        String text = textEditor.getText();

        if (text.isEmpty()) {
            return new int[] {1, 1};
        }

        String[] lines = text.split("\n", -1);
        int row = 1;
        int column = 1;
        int currentPosition = 0;

        for (String line : lines) {
            int lineLength = line.length() + 1;
            if (currentPosition + lineLength > caretPos) {
                column = caretPos - currentPosition + 1;
                break;
            }
            currentPosition += lineLength;
            row++;
        }

        return new int[] {row, column};
    }

    public TextArea getTextEditor() {
        return textEditor;
    }

    public StringProperty textProperty() {
        return textProperty;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
        if (textEditor != null) {
            textEditor.setEditable(!readOnly);
            System.out.println("Read-only mode set to: " + readOnly);
            textEditor.requestFocus();
        } else {
            System.err.println("TextEditor is null in setReadOnly()");
        }
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setCurrentUsername(String username) {
        if (currentUserLabel == null) {
            System.err.println("currentUserLabel is null in setCurrentUsername() - check your editor.fxml file");
            return;
        }

        if (username != null && !username.trim().isEmpty()) {
            currentUserLabel.setText(username + " (you)");
        }
    }

    public void addUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        if (availableUserLabels == null || availableUserLabels.isEmpty()) {
            System.err.println("availableUserLabels is null or empty in addUser()");
            return;
        }

        for (Label userLabel : availableUserLabels) {
            if (userLabel != null && !userLabel.isVisible()) {
                userLabel.setText(username);
                userLabel.setVisible(true);
                break;
            }
        }
    }

    public void removeUser(String username) {
        if (availableUserLabels == null || availableUserLabels.isEmpty()) {
            System.err.println("availableUserLabels is null or empty in removeUser()");
            return;
        }

        for (Label userLabel : availableUserLabels) {
            if (userLabel != null && userLabel.isVisible() && userLabel.getText().equals(username)) {
                userLabel.setVisible(false);
                userLabel.setText("Anonymous " + userLabel.getId());
                break;
            }
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void InitializeDocContents(String DocID, String DocumentName, String CurrentUserName, String Viewer_Code, String Editor_Code) {
        this.documentName = DocumentName;
        this.DocID = DocID;
        this.currentUserLabel.setText(CurrentUserName);
        this.viewerCodeLabel.setText(Viewer_Code);
        this.editorCodeLabel.setText(Editor_Code);
        this.currentUser = new User(CurrentUserName);
        this.sessionCode = Editor_Code;

        try {
            webSocketClient.setOperationHandler(this::handleRemoteOperation);
            webSocketClient.setTextUpdateCallback(textContent -> {
                Platform.runLater(() -> {
                    textEditor.setText(textContent);
                    System.out.println("Text updated from server: " + textContent);
                });
            });
            webSocketClient.connect(this.sessionCode);
        } catch (RuntimeException e) {
            System.err.println("Failed to initialize WebSocket: " + e.getMessage());
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Failed to connect to WebSocket server");
                alert.setContentText("Please ensure the server is running and try again.");
                alert.showAndWait();
            });
        }
    }
    public void setTextArea(String text)
    {
        textEditor.setText(text);
    }

    private List<Label> availableUserLabels;

    public void addComment(ActionEvent actionEvent) {
    }


}