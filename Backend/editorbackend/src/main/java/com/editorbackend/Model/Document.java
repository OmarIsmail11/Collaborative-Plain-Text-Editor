package com.editorbackend.Model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.UUID;

@Data
public class Document {
    private String docID;
    private String DocName;
    private String userName;
    private String viewerCode;
    private String editorCode;
    private String text;
    private String sessionCode;

    @JsonCreator
    public Document(@JsonProperty("docName") String docName, @JsonProperty("userName") String userName, @JsonProperty("text") String text)
    {
        this.docID = docName + "-" + userName + UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        this.DocName = docName;
        this.userName = userName;
    }

//    public Document(String docName, String userName, String text)
//    {
//        this.docID = docName + "-" + userName + UUID.randomUUID().toString().substring(0, 3).toUpperCase();
//        this.DocName = docName;
//        this.userName = userName;
//        this.text = text;
//    }

    public Document() {
    }
}