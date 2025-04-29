package com.example.Controllers;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
    private Button exportButton;

    @FXML
    private Button addCommentButton;

    @FXML
    private VBox commentsContainer;

    private boolean isReadOnly = false;
    private StringProperty textProperty = new SimpleStringProperty(""); // Expose text content as a property

    // Store comments with their text range and index
    private static class Comment {
        String content;
        int startPosition;
        int endPosition;
        String commentedText;
        int index; // Unique index for display (e.g., [1], [2])

        Comment(String content, int startPosition, int endPosition, String commentedText, int index) {
            this.content = content;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.commentedText = commentedText;
            this.index = index;
        }
    }

    private List<Comment> comments = new ArrayList<>();
    private int commentCounter = 0; // For assigning unique indices to comments

    @FXML
    private void initialize() {
        // Bind the textProperty to the TextArea's text
        textProperty.bindBidirectional(textEditor.textProperty());

        // Set read-only mode
        textEditor.setEditable(!isReadOnly);

        // Request focus on TextArea after initialization
        textEditor.requestFocus();

        // Debug: Print initial state
        System.out.println("Editor initialized. Read-only: " + isReadOnly);
    }

    @FXML
    private void addComment() {
        if (isReadOnly) {
            System.out.println("Cannot add comments in read-only mode.");
            return;
        }

        // Check if there’s a selection
        int selectionStart = textEditor.getSelection().getStart();
        int selectionEnd = textEditor.getSelection().getEnd();
        if (selectionStart != selectionEnd) {
            // Prompt the user for the comment content
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Comment");
            dialog.setHeaderText("Enter your comment:");
            dialog.setContentText("Comment:");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(content -> {
                if (!content.trim().isEmpty()) {
                    String commentedText = textEditor.getText(selectionStart, selectionEnd);
                    commentCounter++;
                    Comment comment = new Comment(content, selectionStart, selectionEnd, commentedText, commentCounter);
                    comments.add(comment);
                    displayComment(comment);
                    textEditor.deselect(); // Clear selection after adding comment
                    checkAndRemoveDeletedComments();
                }
            });
        } else {
            System.out.println("Please select text to add a comment.");
        }
    }

    private void displayComment(Comment comment) {
        // Create a container for the comment and delete button
        HBox commentBox = new HBox(5);
        commentBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5; -fx-background-radius: 5;");

        // Create a label for the comment
        Label commentLabel = new Label("[" + comment.index + "] " + comment.content + "\nCommented text: \"" +
                (comment.commentedText.length() > 20 ? comment.commentedText.substring(0, 20) + "..." : comment.commentedText) + "\"");
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-padding: 5;");

        // Add hover effect
        commentLabel.setOnMouseEntered(event -> commentBox.setStyle("-fx-background-color: #d0d0d0; -fx-padding: 5; -fx-background-radius: 5;"));
        commentLabel.setOnMouseExited(event -> commentBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5; -fx-background-radius: 5;"));

        // Add click handler to highlight the commented text
        commentLabel.setOnMouseClicked(event -> {
            int adjustedStart = Math.min(comment.startPosition, textEditor.getText().length());
            int adjustedEnd = Math.min(comment.endPosition, textEditor.getText().length());
            textEditor.selectRange(adjustedStart, adjustedEnd);
            textEditor.requestFocus();
        });

        // Add a delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-font-size: 10px; -fx-background-color: #ff4444; -fx-text-fill: white;");
        deleteButton.setOnAction(event -> {
            comments.remove(comment);
            commentsContainer.getChildren().remove(commentBox);
            checkAndRemoveDeletedComments();
        });

        commentBox.getChildren().addAll(commentLabel, deleteButton);
        commentsContainer.getChildren().add(commentBox);
    }

    private void checkAndRemoveDeletedComments() {
        String text = textEditor.getText();
        Iterator<Comment> iterator = comments.iterator();
        while (iterator.hasNext()) {
            Comment comment = iterator.next();
            int adjustedStart = Math.min(comment.startPosition, text.length());
            int adjustedEnd = Math.min(comment.endPosition, text.length());

            // If the text length is shorter than the comment's start position, it’s deleted
            if (text.length() < comment.startPosition) {
                iterator.remove();
                commentsContainer.getChildren().removeIf(node -> {
                    if (node instanceof HBox) {
                        HBox hbox = (HBox) node;
                        Label label = (Label) hbox.getChildren().get(0);
                        return label.getText().startsWith("[" + comment.index + "]");
                    }
                    return false;
                });
                continue;
            }

            // Extract the current text in the comment's range
            String currentTextInRange;
            try {
                currentTextInRange = text.substring(adjustedStart, adjustedEnd);
            } catch (StringIndexOutOfBoundsException e) {
                currentTextInRange = "";
            }

            // If the text in the range no longer matches the original commented text, remove the comment
            if (!currentTextInRange.equals(comment.commentedText)) {
                iterator.remove();
                commentsContainer.getChildren().removeIf(node -> {
                    if (node instanceof HBox) {
                        HBox hbox = (HBox) node;
                        Label label = (Label) hbox.getChildren().get(0);
                        return label.getText().startsWith("[" + comment.index + "]");
                    }
                    return false;
                });
            }
        }
    }

    @FXML
    private void copyViewerCode() {
        String code = viewerCodeLabel.getText();
        copyToClipboard(code);
        System.out.println("Copied Viewer Code to clipboard: " + code);
    }

    @FXML
    private void copyEditorCode() {
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
        textEditor.undo();
        System.out.println("Undo action triggered");
    }

    @FXML
    private void redoAction() {
        textEditor.redo();
        System.out.println("Redo action triggered");
    }

    @FXML
    private void exportAction() {
        System.out.println("Export action triggered");
    }

    public int getCursorPosition() {
        return textEditor.getCaretPosition();
    }

    public int[] getCursorPositionRowColumn() {
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
        textEditor.setEditable(!readOnly);
        System.out.println("Read-only mode set to: " + readOnly);
        textEditor.requestFocus();
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }
}