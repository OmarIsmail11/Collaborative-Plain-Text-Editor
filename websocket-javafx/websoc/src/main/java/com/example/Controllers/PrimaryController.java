package com.example.Controllers;

import com.example.Model.CRDTNode;
import com.example.Model.CRDTTree;
import com.example.Model.Operation;
import com.example.Model.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.example.config.WebSocketConfig;
import com.example.Service.routeToController;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.text.Caret;

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
    @Autowired
    public WebSocketConfig webSocketClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final ReentrantLock crdtLock = new ReentrantLock();
    private Set<String> localOperationIds = new HashSet<>();
    private volatile boolean isUpdating = false;
    private final Object uiUpdateLock = new Object();
    private int lastCaretPos = 0;
    private final ScheduledExecutorService displayScheduler = Executors.newSingleThreadScheduledExecutor();
    private Map<String, Integer> userCursorLines = new HashMap<>(); // Track user cursor positions
    private int lastSentLine = -1; // Track last sent line to avoid duplicate sends
    Object cursorLock = new Object();


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

        textEditor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                undoAction();
                event.consume();
            }
        });

        textEditor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.Z) {
                redoAction(); // Call your redo method here
                event.consume(); // Prevent further propagation of this key event
            }
        });
//
//        textEditor.caretPositionProperty().addListener(new ChangeListener<Integer>() {
//            @Override
//            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
//                if (newValue != null) {
//                    handleCursorChange(newValue);
//                } else {
//                    System.err.println("Caret position is null");
//                }
//            }
//        });


        Platform.runLater(() -> textEditor.requestFocus());

        // Debug: Print initial state and check if UI elements are properly initialized
        System.out.println("Editor initialized. Read-only: " + isReadOnly);
        printInitializationStatus();

    }
    // Handle cursor position change
    private void handleCursorChange(int caretPosition) {
        int lineNumber = getLineNumberFromCaret(caretPosition);
        if (lineNumber != lastSentLine) { // Only send if line changed
            lastSentLine = lineNumber;
            Operation cursorOp = new Operation("cursor", lineNumber, currentUser.getUserID());
            webSocketClient.sendOperation(sessionCode, cursorOp);
            localOperationIds.add(cursorOp.getId());
            // Update local user's cursor position
            userCursorLines.put(currentUser.getUserID(), lineNumber);
            updateCursorDisplay();
        }
    }

    // Update UI to display all users' cursor positions (runs every 500ms)
    private void updateCursorDisplay() {
        StringBuilder displayText = new StringBuilder("Cursor Positions:\n");

        synchronized (cursorLock) {
            userCursorLines.forEach((userID, line) -> {
                displayText.append("User ").append(userID).append(": Line ").append(line).append("\n");
            });
        }
        // Update JavaFX UI on the JavaFX Application Thread
        //Platform.runLater(() -> cursorInfoLabel.setText(displayText.toString()));
    }

    private int getLineNumberFromCaret(int caretPosition) {
        String text = textEditor.getText();
        if (text.isEmpty() || caretPosition <= 0) {
            return 1;
        }
        int lineCount = 1;
        for (int i = 0; i < caretPosition && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCount++;
            }
        }
        return lineCount;
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
        if(!this.isReadOnly) {
            if (event.getCode() == KeyCode.ENTER) {
                int caretPos = textEditor.getCaretPosition();
                String nodeId = currentUser.getUserID() + "_" + LocalDateTime.now().toString();

                localOperationIds.add(nodeId);
                CRDTNode newNode = new CRDTNode(
                        nodeId, '\n', LocalDateTime.now().toString(), false, null, currentUser.getUserID(), caretPos
                );

                synchronized (crdtLock) {
                    crdtTree.insert(newNode);
                    currentUser.addToUndoStack("insert", newNode, caretPos);
                }

                Operation operation = new Operation("insert", newNode, caretPos);
                operation.setId(nodeId);

                executorService.submit(() -> {
                    try {
                        webSocketClient.sendOperation(sessionCode, operation);
                        System.out.println("Sent operation: insert newline at position: " + caretPos);
                    } catch (Exception e) {
                        System.err.println("Failed to send operation: " + e.getMessage());
                    }
                });

                updateTextEditorContent(caretPos + 1);
                event.consume();
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                int caretPos = textEditor.getCaretPosition();
                if (caretPos > 0 && caretPos <= crdtTree.visibleNodes.size()) {
                    int deletePos = caretPos - 1;
                    CRDTNode delNode;
                    synchronized (crdtLock) {
                        delNode = crdtTree.visibleNodes.get(deletePos);
                        crdtTree.delete(deletePos, currentUser.getUserID());
                        currentUser.addToUndoStack("delete", delNode, deletePos);
                    }

                    String operationId = "delete_" + delNode.getId() + "_" + System.currentTimeMillis();
                    localOperationIds.add(operationId);
                    Operation operation = new Operation("delete", delNode, deletePos);
                    operation.setId(operationId);

                    executorService.submit(() -> {
                        try {
                            webSocketClient.sendOperation(sessionCode, operation);
                            System.out.println("Sent operation: delete at position: " + deletePos);
                        } catch (Exception e) {
                            System.err.println("Failed to send operation: " + e.getMessage());
                        }
                    });

                    updateTextEditorContent(deletePos);
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.DELETE) {
                int caretPos = textEditor.getCaretPosition();
                if (caretPos < crdtTree.visibleNodes.size()) {
                    CRDTNode delNode;
                    synchronized (crdtLock) {
                        delNode = crdtTree.visibleNodes.get(caretPos);
                        crdtTree.delete(caretPos, currentUser.getUserID());
                        currentUser.addToUndoStack("delete", delNode, caretPos);
                    }

                    String operationId = "delete_" + delNode.getId() + "_" + System.currentTimeMillis();
                    localOperationIds.add(operationId);
                    Operation operation = new Operation("delete", delNode, caretPos);

                    operation.setId(operationId);

                    executorService.submit(() -> {
                        try {
                            webSocketClient.sendOperation(sessionCode, operation);
                            System.out.println("Sent operation: delete at position: " + caretPos);
                        } catch (Exception e) {
                            System.err.println("Failed to send operation: " + e.getMessage());
                        }
                    });

                    updateTextEditorContent(caretPos);
                    event.consume();
                }
            }
        }
    }
    private void handleKeyTyped(KeyEvent event) {
        if(!this.isReadOnly) {
            String character = event.getCharacter();
            if (character.length() == 0 ||
                    (character.codePointAt(0) < 32 && character.codePointAt(0) != 10) || // Allow newline (10)
                    character.equals("\b") || character.equals("\u007F") ||
                    character.equals("\n")) { // Skip newline in keyTyped since we handle it in keyPressed
                return;
            }

            int caretPos = textEditor.getCaretPosition();
            for (int i = 0; i < character.length(); i++) {
                char ch = character.charAt(i);
                String nodeId = currentUser.getUserID() + "_" + LocalDateTime.now().toString();

                localOperationIds.add(nodeId);
                CRDTNode newNode = new CRDTNode(
                        nodeId, ch, LocalDateTime.now().toString(), false, null, currentUser.getUserID(), caretPos + i
                );

                synchronized (crdtLock) {
                    crdtTree.insert(newNode);
                    currentUser.addToUndoStack("insert", newNode, caretPos + i);
                }

                Operation operation = new Operation("insert", newNode, caretPos + i);
                operation.setId(nodeId);

                executorService.submit(() -> {
                    try {
                        webSocketClient.sendOperation(sessionCode, operation);

                    } catch (Exception e) {
                        System.err.println("Failed to send operation: " + e.getMessage());
                    }
                });
                System.out.println("Sent operation: insert at position: " + (caretPos + i));

                crdtTree.printCRDTTree();
                crdtTree.printText();
            }


            updateTextEditorContent(caretPos + character.length());
            event.consume();
        }
    } // Method to update the text editor content based on CRDT state with specific caret position
    private void updateTextEditorContent(int newCaretPos) {
        synchronized (uiUpdateLock) {
            if (isUpdating) {
                System.out.println("Skipping UI update: already updating");
                return; // Skip if already updating to prevent queuing multiple updates
            }
            isUpdating = true;
        }

        try {
            StringBuilder content = new StringBuilder();
            crdtLock.lock();


            try {
                for (CRDTNode node : crdtTree.getVisibleNodes()) {
                    content.append(node.getValue());
                    System.out.println("text = " + content.toString());
                }
            } finally {
                crdtLock.unlock();
            }

            int safeCaretPos = Math.min(Math.max(newCaretPos, 0), content.length());
            lastCaretPos = safeCaretPos;

            Platform.runLater(() -> {
                synchronized (uiUpdateLock) {
                    textEditor.setText(content.toString());
                    textEditor.positionCaret(lastCaretPos);
                    System.out.println("UI updated. Content length: " + content.length() + ", Caret at: " + lastCaretPos);
                    isUpdating = false;
                }
            });
        } catch (Exception e) {
            System.err.println("Error updating UI: " + e.getMessage());
            synchronized (uiUpdateLock) {
                isUpdating = false;
            }
        }
    }

    // Overloaded method that uses current caret position
    private void updateTextEditorContent() {
        int caretPos = textEditor.getCaretPosition();
        updateTextEditorContent(caretPos);
    }



    public void handleRemoteOperation(Operation operation) {

        if (localOperationIds.contains(operation.getId())) {
            System.out.println("Skipping local operation echo: " + operation.getType());
            localOperationIds.remove(operation.getId());
            return;
        }

        executorService.submit(() -> {
            try {
                crdtLock.lock();

                try {
                    System.out.println("Processing remote operation: " + operation.getType());
                    if ("insert".equals(operation.getType())) {
                        if (operation.getNode() == null) {
                            System.err.println("Insert operation has null node");
                            return;
                        }
                        crdtTree.insert(operation.getNode());
                        crdtTree.printCRDTTree();
                    } else if ("delete".equals(operation.getType())) {
                        if (operation.getNode() == null || operation.getIndex() < 0 || operation.getIndex() >= crdtTree.getVisibleNodes().size()) {
                            System.err.println("Invalid delete operation: index=" + operation.getIndex());
                            return;
                        }
                        crdtTree.delete(operation.getIndex(), operation.getNode().getUserID());
                        crdtTree.printCRDTTree();
                    } else if ("undo".equals(operation.getType())) {
                        if (operation.getNode() == null) {
                            System.err.println("Undo operation has null node");
                            return;
                        }
                        System.out.println("Searching for node with ID: " + operation.getNode().getId());
                        System.out.println("Node list IDs: " + crdtTree.getNodeList().stream().map(CRDTNode::getId).collect(Collectors.joining(", ")));
                        boolean found = false;
                        for (CRDTNode node : crdtTree.getNodeList()) {
                            if (operation.getNode().getId().equals(node.getId())) {
                                if ("insert".equals(operation.getOriginalType())) {
                                    node.setDeleted(true);
                                    this.crdtTree.rebuildFromNodeList();
                                    System.out.println("Insert operation for " + node.getId() + " undone");
                                } else if ("delete".equals(operation.getOriginalType())) {
                                    node.setDeleted(false);
                                    this.crdtTree.rebuildFromNodeList();
                                    System.out.println("Delete operation for " + node.getId() + " undone");
                                } else {
                                    System.err.println("Invalid original operation type for undo: " + operation.getOriginalType() + " for node ID: " + node.getId());
                                    return;
                                }
                                crdtTree.printCRDTTree();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            System.err.println("No matching node found for undo with ID: " + operation.getNode().getId());
                        }
                    } else if ("redo".equals(operation.getType())) {
                        if (operation.getNode() == null) {
                            System.err.println("Redo operation has null node");
                            return;
                        }
                        System.out.println("Searching for node with ID: " + operation.getNode().getId());
                        System.out.println("Node list IDs: " + crdtTree.getNodeList().stream().map(CRDTNode::getId).collect(Collectors.joining(", ")));
                        boolean found = false;
                        for (CRDTNode node : crdtTree.getNodeList()) {
                            if (operation.getNode().getId().equals(node.getId())) {
                                if ("insert".equals(operation.getOriginalType())) {
                                    node.setDeleted(false);
                                    this.crdtTree.rebuildFromNodeList();
                                    System.out.println("Insert operation for " + node.getId() + " redone");
                                } else if ("delete".equals(operation.getOriginalType())) {
                                    node.setDeleted(true);
                                    this.crdtTree.rebuildFromNodeList();
                                    System.out.println("Delete operation for " + node.getId() + " redone");
                                } else {
                                    System.err.println("Invalid original operation type for redo: " + operation.getOriginalType() + " for node ID: " + node.getId());
                                    return;
                                }
                                crdtTree.printCRDTTree();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            System.err.println("No matching node found for redo with ID: " + operation.getNode().getId());
                        }
                    } else {
                        System.err.println("Unknown operation type: " + operation.getType());
                        return;
                    }
                } finally {
                    crdtLock.unlock();
                }

                updateTextEditorContent();

            } catch (Exception e) {
                System.err.println("Error processing remote operation: " + e.getMessage());
            }
        });
    }

    @FXML
    private void undoAction() {
        if (textEditor == null) {
            System.err.println("TextEditor is null in undoAction()");
            return;
        }
        crdtLock.lock();
        try {
            if (!currentUser.getUndoStack().isEmpty()) {
                Operation op = currentUser.undo(this.crdtTree);
                if (op != null) {
                    this.crdtTree.rebuildFromNodeList();
                    this.crdtTree.printCRDTTree();
                    updateTextEditorContent();
                    System.out.println("Undid " + op.getOriginalType() + " on node " + op.getNode().getId());
                    op.getNode().printNode();
                    op.setType("undo");
                    webSocketClient.sendOperation(this.sessionCode, op);
                    localOperationIds.add(op.getId());
                } else {
                    System.out.println("Nothing to undo");
                }
            } else {
                System.out.println("Nothing to undo");
            }
        } finally {
            crdtLock.unlock();
        }
        System.out.println("Undo action triggered");
    }

    @FXML
    private void redoAction() {
        if (textEditor == null) {
            System.err.println("TextEditor is null in redoAction()");
            return;
        }
        crdtLock.lock();
        try {
            Operation op = currentUser.redo(this.crdtTree);
            if (op != null) {
                this.crdtTree.rebuildFromNodeList();
                this.crdtTree.printCRDTTree();
                updateTextEditorContent();
                System.out.println("Redid " + op.getOriginalType() + " on node " + op.getNode().getId());
                op.getNode().printNode();
                op.setType("redo");
                webSocketClient.sendOperation(this.sessionCode, op);
                localOperationIds.add(op.getId());
            } else {
                System.out.println("Nothing to redo");
            }
        } finally {
            crdtLock.unlock();
        }
        System.out.println("Redo action triggered");
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

    // Method 1: Manual Initialization in InitializeDocContents
    public void InitializeDocContents(String DocID, String DocumentName, String CurrentUserName, String Viewer_Code, String Editor_Code) {
        this.documentName = DocumentName;
        this.DocID = DocID;
        this.currentUserLabel.setText(CurrentUserName);
        this.viewerCodeLabel.setText(Viewer_Code);
        this.editorCodeLabel.setText(Editor_Code);
        this.currentUser = new User(CurrentUserName);
        this.sessionCode = Editor_Code;

        // Initialize webSocketClient manually if it's null
        if (this.webSocketClient == null) {
            this.webSocketClient = new WebSocketConfig();
        }

        try {
            webSocketClient.setUserManagement(this::addUser);
            webSocketClient.setOperationHandler(this::handleRemoteOperation);
            webSocketClient.setTextUpdateCallback(textContent -> {
                Platform.runLater(() -> {
                    textEditor.setText(textContent);
                    System.out.println("Text updated from server: " + textContent);
                });
            });
            webSocketClient.setCRDTTreeInitializer(tree -> {
                this.crdtTree = tree;
                updateTextEditorContent();
                System.out.println("Received new tree");
                this.crdtTree.printCRDTTree();
                this.crdtTree.printText();
            });

            // Connect to WebSocket and subscribe to initial state
            webSocketClient.connect(this.sessionCode);

            webSocketClient.subscribeToInitialState(this.sessionCode,currentUser.getUserID());
            webSocketClient.requestInitialState(this.sessionCode,currentUser.getUserID());


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

    private void addUser(User user) {

    }


    public void setTextArea(String text)
    {
        textEditor.setText(text);
    }

    private List<Label> availableUserLabels;

    public void addComment(ActionEvent actionEvent) {
    }

    //TODO
    //BUTTON TO GO BACK
    // HANDLER FOR BUTTON
    // TERMINATE WEBSOCKET CONNECTION
    // ACTIVE USERS LIST ON JOIN
    // WEBSOCKET FOR JOINING /app/joined/{userID}
    //broadcast
    //add user to users list
    //this.weboscketClient.close();

}