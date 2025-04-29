package com.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

public class NewDocumentDialogController {

    @FXML
    private TextField documentNameField;

    @FXML
    private TextField usernameField;

    private String documentName;
    private String username;

    @FXML
    private void initialize() {
        // Initialize if needed
    }

    // Called when the dialog is closed to retrieve the document name
    public String getDocumentName() {
        return documentName;
    }

    // Called when the dialog is closed to retrieve the username
    public String getUsername() {
        return username;
    }

    // Called by the dialog's result converter
    public void processResult(ButtonType buttonType) {
        if (buttonType.getButtonData().isDefaultButton()) { // Create button
            documentName = documentNameField.getText();
            username = usernameField.getText();
        } else {
            documentName = null;
            username = null;
        }
    }
}