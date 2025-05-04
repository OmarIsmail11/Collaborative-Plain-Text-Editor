package com.example.Model;

import com.example.Model.CRDTNode;

public class Operation {
    private String type;
    private CRDTNode node;
    private int index;
    private String id;
    

    // Default constructor required for Jackson deserialization
    public Operation() {
    }

    public Operation(String type, CRDTNode node, int index) {
        this.type = type;
        this.node = node;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CRDTNode getNode() {
        return node;
    }

    public void setNode(CRDTNode node) {
        this.node = node;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}