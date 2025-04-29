package com.editorbackend.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;


@Service
public class DocumentRegistry {
    private Map<String, Document> documents = new HashMap<>();

    

    public Document createDocument(String docName) {
        Document doc = new Document(docName);
        doc.setViewerCode(generateCode("VIEW"));
        doc.setEditorCode(generateCode("EDIT"));
        doc.setText("");
        
        documents.put(doc.getDocName(), doc);
        return doc;
    }

    private String generateCode(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Document getDocument(String docId) {
        return documents.get(docId);
    }
    public Document getDocumentByCode(String code) {
        for (Document doc : documents.values()) {
            if (doc.getViewerCode().equals(code) || doc.getEditorCode().equals(code)) {
                return doc;
            }
        }
        return null;
    }
}