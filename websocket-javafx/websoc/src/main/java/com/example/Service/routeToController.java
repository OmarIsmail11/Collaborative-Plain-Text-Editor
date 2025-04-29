package com.example.Service;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class routeToController {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public routeToController() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8080/";
    }

    // Class to match backend's CreateDocumentRequest
    public static class CreateDocumentRequest {
        private String name;
        private String author;

        public CreateDocumentRequest(String name, String author) {
            this.name = name;
            this.author = author;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }

    public Document createNewDocument(String docName, String username) {
        String url = baseUrl + "editor/create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request payload
        CreateDocumentRequest request = new CreateDocumentRequest(docName, username);
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