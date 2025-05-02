package com.example.Model;


public class Operation {
    private String type;
    private CRDTNode node;
    private int index;
    private String userID;

    public Operation(String type, CRDTNode node, int index) {
        this.type = type;
        this.node = node;
        this.index = index;
    }

    public String getType() { return type; }
    public CRDTNode getNode() { return node; }
    public int getIndex() { return index; }
    public String getUserID() { return userID; }

}

