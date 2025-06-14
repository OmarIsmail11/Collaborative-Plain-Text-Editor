package com.editorbackend.CRDT;



public class Operation {
    private String type;
    private CRDTNode node;
    private String OriginalType;
    private int index;
    private String UserID;
    private int LineNumber;


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

    public void setOriginalType(String OriginalType) {
        this.OriginalType = OriginalType;
    }

    public String getOriginalType() {
        return OriginalType;
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


    public String UserID() {
        return UserID;
    }

    public void setUserID(String id) {
        this.UserID = id;
    }

    public int getLineNumber() {
        return LineNumber;

    }

    public void setLineNumber(int line) {
        this.LineNumber = line;
    }

}
// insert 
// push --> undo 
// pop--> insert setdeleted 
//