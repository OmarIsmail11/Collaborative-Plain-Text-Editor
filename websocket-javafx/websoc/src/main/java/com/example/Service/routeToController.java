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

    public void createNewDocument(String docName, String username) {
        String url = baseUrl + "editor/create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        DocumentRequest request = new DocumentRequest(docName, username);
        HttpEntity<DocumentRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Response: " + response.getBody());
    }
}