package com.editorbackend.CRDT;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CRDTNode {
    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private char value;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

    @JsonProperty("parent")
    private CRDTNode parent;

    @JsonProperty("nextNodes")
    private List<CRDTNode> nextNodes;

    @JsonProperty("userID")
    private String userID;

    @JsonProperty("index")
    private int index;

    public CRDTNode() {
        this.nextNodes = new ArrayList<>();
    }

    public CRDTNode(char value, String timestamp, String userID, int index) {
        this.id = userID + "_" + timestamp;
        this.value = value;
        this.timestamp = timestamp;
        this.nextNodes = new ArrayList<>();
        this.userID = userID;
        this.index = index;
    }

    public CRDTNode(String id, char value, String timestamp, boolean isDeleted, CRDTNode parent, String userID, int index) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
        this.isDeleted = isDeleted;
        this.parent = parent;
        this.nextNodes = new ArrayList<>();
        this.userID = userID;
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public CRDTNode getParent() {
        return parent;
    }

    public void setParent(CRDTNode parent) {
        this.parent = parent;
    }

    public List<CRDTNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<CRDTNode> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public void addNextNode(CRDTNode nextNode) {
        this.nextNodes.add(nextNode);
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void printNode() {
        System.out.println("Node ID: " + id + ", Value: " + value + ", Timestamp: " + timestamp +
                ", Deleted: " + isDeleted + ", Index: " + index + ", UserID: " + userID +
                ", Parent ID: " + (parent != null ? parent.getId() : "null"));
    }

    @Override
    public String toString() {
        return "CRDTNode{id='" + id + "', value=" + value + ", timestamp='" + timestamp + "', isDeleted=" + isDeleted +
                ", userID='" + userID + "', index=" + index + "}";
    }
}