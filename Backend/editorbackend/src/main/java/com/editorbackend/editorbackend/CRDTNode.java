package main.java.com.editorbackend.editorbackend;

import java.sql.Time;

public class CRDTNode{

    private String id; //Node ID
    private String Value; //ASCII CHARACTER
    private Time timestamp;
    private boolean isDeleted;
    private String parentId; //Parent ID
    private String NextNode;



    public CRDTNode(String id, String value, Time timestamp, boolean isDeleted, String parentId, String nextNode) {
        this.id = id;
        Value = value;
        this.timestamp = timestamp;
        this.isDeleted = isDeleted;
        this.parentId = parentId;
    }

    //Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public Time getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Time timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDeleted() {
        return isDeleted;
    }   

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    public String getNextNode() {
        return NextNode;
    }
    public void setNextNode(String nextNode) {
        NextNode = nextNode;
    }
}