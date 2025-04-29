package com.editorbackend.Model;

import lombok.Data;

@Data
public class Document {
    private String DocName;
    private String viewerCode;
    private String editorCode;
    private String text;

    public Document(String Docname){
        this.DocName = Docname;
    }
}