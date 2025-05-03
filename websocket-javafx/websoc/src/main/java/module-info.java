module com.example {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // Spring Boot modules
    requires spring.web;
    requires spring.context;

    // Additional modules
    requires spring.messaging;
    requires spring.websocket;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires spring.beans;

    // Open the package for reflection (used by JavaFX and Spring Boot)
    opens com.example.Controllers to javafx.fxml;
    opens com.example.Service to javafx.fxml, spring.web, com.fasterxml.jackson.databind;

    // Export the package
    exports com.example;
    exports com.example.Controllers;
    exports com.example.Service;
    exports com.example.Model;
    opens com.example.Model to com.fasterxml.jackson.databind, javafx.fxml, spring.web;
}