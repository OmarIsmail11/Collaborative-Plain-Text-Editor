package com.example.Model;
import com.example.Model.CRDTNode;
import com.example.Model.CRDTTree;
import com.example.Model.Operation;

import java.util.Stack;
import java.util.stream.Collectors;

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
        op.setOriginalType(op.getType()); // Preserve original type (insert or delete)
        op.setType("undo");
        redoStack.push(op);

        if ("insert".equals(op.getOriginalType())) {
            op.getNode().setDeleted(true);
        } else if ("delete".equals(op.getOriginalType())) {
            op.getNode().setDeleted(false);
        }
        return op;
    }

    public Operation redo(CRDTTree crdtTree) {
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo for user: " + userID);
            return null;
        }

        Operation op = redoStack.pop();
        // OriginalType is already set by undo; do not overwrite
        op.setType("redo");
        undoStack.push(op);

        if ("insert".equals(op.getOriginalType())) {
            op.getNode().setDeleted(false);
        } else if ("delete".equals(op.getOriginalType())) {
            op.getNode().setDeleted(true);
        }
        return op;
    }

    public void printUndoStack() {
        for (Operation op : undoStack) {
            op.getNode().printNode();
        }
    }

    public Stack<Operation> getUndoStack() {
        return this.undoStack;
    }
}