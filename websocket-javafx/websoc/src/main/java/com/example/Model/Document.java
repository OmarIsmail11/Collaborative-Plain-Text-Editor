package com.example.Model;

import javax.print.Doc;

public class Document {
    private String docID;
    private String userName;
    private String DocName;
    private String viewerCode;
    private String editorCode;
    private String text;

    // Default constructor for Jackson serialization
    public Document() {}

    public Document(String docID, String userName, String DocName, String viewerCode, String editorCode, String text) {
        this.docID = DocName + "/" + userName;
        this.userName = userName;
        this.DocName = DocName;
        this.viewerCode = viewerCode;
        this.editorCode = editorCode;
        this.text = text;
    }
    public Document(String DocName) {
        this.DocName = DocName;
    }

    // Getters and Setters
    public String getDocName() { return DocName; }
    public void setDocName(String DocName) { this.DocName = DocName; }
    public String getViewerCode() { return viewerCode; }
    public void setViewerCode(String viewerCode) { this.viewerCode = viewerCode; }
    public String getEditorCode() { return editorCode; }
    public void setEditorCode(String editorCode) { this.editorCode = editorCode; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}