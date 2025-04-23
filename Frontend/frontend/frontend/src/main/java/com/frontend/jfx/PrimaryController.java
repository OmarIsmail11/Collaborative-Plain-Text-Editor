package com.frontend.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

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
    private void copyViewerCode() {
        String code = viewerCodeLabel.getText();
        System.out.println("Copied Viewer Code: " + code);
        // Optionally, add clipboard functionality here
    }

    @FXML
    private void copyEditorCode() {
        String code = editorCodeLabel.getText();
        System.out.println("Copied Editor Code: " + code);
        // Optionally, add clipboard functionality here
    }

    @FXML
    private void undoAction() {
        System.out.println("Undo action triggered");
        // Implement undo functionality here
    }

    @FXML
    private void redoAction() {
        System.out.println("Redo action triggered");
        // Implement redo functionality here
    }

    @FXML
    private void exportAction() {
        System.out.println("Export action triggered");
        // Implement export functionality here
    }

    public int getCursorPosition() {
        return textEditor.getCaretPosition();
    }

    /**
     * Returns the cursor position as a row and column pair.
     * Row and column numbers are 1-based (i.e., the first row/column is 1, not 0).
     * @return an array of two integers [row, column]
     */
    public int[] getCursorPositionRowColumn() {
        int caretPosition = textEditor.getCaretPosition();
        String text = textEditor.getText();

        // Handle empty text
        if (text == null || text.isEmpty()) {
            return new int[] {1, 1}; // Row 1, Column 1
        }

        // Split the text into lines
        String[] lines = text.split("\n", -1); // -1 to include trailing empty lines
        int row = 1;
        int column = 1;
        int currentPosition = 0;

        // Iterate through each line to find the cursor's row and column
        for (String line : lines) {
            int lineLength = line.length() + 1; // +1 for the newline character
            if (currentPosition + lineLength > caretPosition) {
                column = caretPosition - currentPosition + 1; // +1 to make column 1-based
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
}