package com.example.Model;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



public class CRDTNode {
    private String id;
    private char value;
    private LocalDateTime timestamp;
    private boolean isDeleted;
    private CRDTNode parent;
    private List<CRDTNode> nextNodes;
    private String UserID;
    private int index;

    public CRDTNode(String id, char value, LocalDateTime timestamp, boolean isDeleted, CRDTNode parent, String UserID, int index) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
        this.isDeleted = isDeleted;
        this.parent = parent;
        this.nextNodes = new ArrayList<>();
        this.UserID = UserID;
        this.index = index;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public char getValue() { return value; }
    public void setValue(char value) { this.value = value; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public CRDTNode getParent() { return parent; }
    public void setParent(CRDTNode parent) { this.parent = parent; }
    public List<CRDTNode> getNextNodes() { return nextNodes; }
    public void addNextNode(CRDTNode nextNode) { this.nextNodes.add(nextNode); }
    public String getUserID() { return UserID; }
    public void setUserID(String userID) { UserID = userID; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public void printNode() {
        System.out.println("Node ID: " + id + ", Value: " + value + ", Timestamp: " + timestamp +
                ", Deleted: " + isDeleted + ", Index: " + index + ", UserID: " + UserID +
                ", Parent ID: " + (parent != null ? parent.getId() : "null"));
    }
}