package com.editorbackend.CRDT;



public class Operation {
    private String type;
    private CRDTNode node;
    private int index;
    private String id;

    public Operation(String type, CRDTNode node, int index) {
        this.type = type;
        this.node = node;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public CRDTNode getNode() {
        return node;
    }

    public int getIndex() {
        return index;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
// insert 
// push --> undo 
// pop--> insert setdeleted 
//