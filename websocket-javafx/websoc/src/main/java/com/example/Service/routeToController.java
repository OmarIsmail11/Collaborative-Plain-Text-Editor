package com.example.Service;

import com.example.Model.Document;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class routeToController {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public routeToController() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8080/";
    }

    public Document getDocumentID(String sessionCode) {
        String url = baseUrl + "editor/get/"+sessionCode;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Document> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Document.class
            );

            Document doc = response.getBody();
            if (doc != null) {
                System.out.println("Fetched document: " + doc.getDocName());
                return doc;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching document: " + e.getMessage());
            return null;
        }

    }

    public Document saveDocument(String docID, String text) {
        String url = baseUrl + "editor/update/"+docID;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));


        Map<String, String> request = new HashMap<>();
        request.put("text", text);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);



        try {
            ResponseEntity<Document> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Document.class
            );

            Document doc = response.getBody();
            if (doc != null) {
                System.out.println("Fetched document: " + doc.getDocName());
                return doc;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching document: " + e.getMessage());
            return null;
        }
    }


    public static class UpdateDocumentRequest {
        private String text;
        public String getText() { return text; }
        // public void setText(String text) { this.text = text; }
    }


    // Class to match backend's CreateDocumentRequest
    public static class CreateDocumentRequest {
        private String name;
        private String author;
        private String text;

        public CreateDocumentRequest(String name, String author, String text) {
            this.name = name;
            this.author = author;
            this.text = text;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public Document createNewDocument(String docName, String username, String text) {
        String url = baseUrl + "editor/create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request payload
        CreateDocumentRequest request = new CreateDocumentRequest(docName, username, text);
        HttpEntity<CreateDocumentRequest> entity = new HttpEntity<>(request, headers);

        try {
            // Send POST request and expect a Document response
            ResponseEntity<Document> response = restTemplate.postForEntity(url, entity, Document.class);
            Document newDoc = response.getBody();
            if (newDoc != null) {
                System.out.println("Created document: " + newDoc.getDocName() +
                        ", ViewerCode: " + newDoc.getViewerCode() +
                        ", EditorCode: " + newDoc.getEditorCode());
                return newDoc;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error creating document: " + e.getMessage());
            return null;
        }

    }

}