package com.editorbackend.Controllers;
import com.editorbackend.Model.*;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/editor")  // Add this line
public class RestControllerHome {
    // baseUrl = "http://localhost:8080/";
    
    @Autowired
    private DocumentRegistry documentRegistry;


    public static class CreateDocumentRequest {
        private String name;
        private String author;
        
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }

    @GetMapping("/get/{code}")
    public ResponseEntity<Document> LoadDocument(@PathVariable String code) {
        Document doc = documentRegistry.getDocumentByCode(code);
        
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(doc);
    }
    @PostMapping("/create")
    public ResponseEntity<Document> createDocument(@RequestBody CreateDocumentRequest request) {
        Document newDoc = documentRegistry.createDocument(request.getName());
        
        return ResponseEntity.ok(newDoc);
    }
}