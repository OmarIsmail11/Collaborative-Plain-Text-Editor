package com.example.Model;

import java.util.Stack;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Stack;

@JsonInclude(JsonInclude.Include.NON_NULL)  // Exclude null fields from JSON serialization
public class User {

    @JsonProperty("userID") // Explicitly define the property name in JSON if needed
    private String userID;

    @JsonProperty("undoStack")
    private Stack<Operation> undoStack;

    @JsonProperty("redoStack")
    private Stack<Operation> redoStack;

    // Default constructor for Jackson
    public User() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    // Constructor with userID
    public User(String userID) {
        this.userID = userID;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    // Getter for userID
    public String getUserID() {
        return userID;
    }

    // Setter for userID
    public void setUserID(String userID) {
        this.userID = userID;
    }

    // Getter for undoStack
    public Stack<Operation> getUndoStack() {
        return undoStack;
    }

    // Setter for undoStack
    public void setUndoStack(Stack<Operation> undoStack) {
        this.undoStack = undoStack;
    }

    // Getter for redoStack
    public Stack<Operation> getRedoStack() {
        return redoStack;
    }

    // Setter for redoStack
    public void setRedoStack(Stack<Operation> redoStack) {
        this.redoStack = redoStack;
    }

    // Method to add operation to undo stack
    public void addToUndoStack(String type, CRDTNode node, int index) {
        undoStack.push(new Operation(type, node, index));
        redoStack.clear();
    }

    // Undo operation
    public Operation undo(CRDTTree crdtTree) {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo for user: " + userID);
            return null;
        }

        Operation op = undoStack.pop();
        redoStack.push(op);
        if (op.getType().equals("insert")) op.getNode().setDeleted(true);
        else if (op.getType().equals("delete")) op.getNode().setDeleted(false);
        return op;
    }

    // Redo operation
    public Operation redo(CRDTTree crdtTree) {
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo for user: " + userID);
            return null;
        }

        Operation op = redoStack.pop();
        undoStack.push(op);
        if (op.getType().equals("insert")) op.getNode().setDeleted(false);
        else if (op.getType().equals("delete")) op.getNode().setDeleted(true);
        return op;
    }
}

