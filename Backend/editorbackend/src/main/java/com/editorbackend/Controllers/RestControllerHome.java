package com.editorbackend.Controllers;

import com.editorbackend.Model.*;
import com.editorbackend.CRDTService.CRDTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/editor")
public class RestControllerHome {
    @Autowired
    private DocumentRegistry documentRegistry;

    @Autowired
    private CRDTService crdtService;

    public static class CreateDocumentRequest {
        private String name;
        private String author;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }

    public static class UpdateDocumentRequest {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }


    @GetMapping("/get/{code}")
    public ResponseEntity<Document> LoadDocument(@PathVariable String code) {
        Document doc = documentRegistry.getDocumentByCode(code);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }
        // Update text using CRDTService
        doc.setText(crdtService.getState(code));
        return ResponseEntity.ok(doc);
    }

    @PostMapping("/create")
    public ResponseEntity<Document> createDocument(@RequestBody CreateDocumentRequest request) {
        Document newDoc = documentRegistry.createDocument(request.getName(), request.getAuthor());
        return ResponseEntity.ok(newDoc);
    }


    @PostMapping("/update/{code}")
    public ResponseEntity<Document> updateDocument(@PathVariable String code, @RequestBody UpdateDocumentRequest body) {
        Document doc = documentRegistry.getDocumentByID(code);
        String newText = body.getText();
        System.out.println("THIS IS TEXT"+ newText);
        doc.setText(newText);
        documentRegistry.saveDocuments();

        return ResponseEntity.ok(doc);
    }



}