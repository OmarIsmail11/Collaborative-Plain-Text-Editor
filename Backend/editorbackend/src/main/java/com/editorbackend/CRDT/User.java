package com.editorbackend.CRDT;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.*;
import java.net.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.lang.String;
import java.lang.Thread;



public class User {
    private String userID;

    private Stack<Operation> undoStack;
    private Stack<Operation> redoStack;

    public User(String userID) {
        this.userID = userID;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public String getUserID() { return userID; }
    public void addToUndoStack(String type, CRDTNode node, int index) {
        undoStack.push(new Operation(type, node, index));
        redoStack.clear();
    }

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

