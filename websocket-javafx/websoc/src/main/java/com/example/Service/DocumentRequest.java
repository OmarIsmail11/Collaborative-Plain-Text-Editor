package com.example.Service;

public class DocumentRequest {
    private String name;
    private String author;

    public DocumentRequest() {} // Default constructor for Jackson

    public DocumentRequest(String name, String author) {
        this.name = name;
        this.author = author;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}