package com.example.Service;



public class Document {
    private String DocName;
    private String viewerCode;
    private String editorCode;
    private String text;

    // Default constructor for Jackson serialization
    public Document() {}

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