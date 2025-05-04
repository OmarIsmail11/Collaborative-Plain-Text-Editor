package com.editorbackend.Model;

import com.editorbackend.CRDT.CRDTTree;
import com.editorbackend.CRDT.User;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
public class DocumentRegistry {
    private static final String filePath = "editorbackend/data/documents.json";  // Ensure "Database" directory exists
    private Map<String, Document> documents = new HashMap<>();
    private Map<String, CRDTTree> crdtTrees = new HashMap<>();
    private Map<String, Map<String, User>> sessionUsers = new HashMap<>();
    private ObjectMapper objectMapper;

    public DocumentRegistry() {
        objectMapper = new ObjectMapper();
        loadDocuments();
    }

    // docID is a combination of docName and userName separated by -
    public Document createDocument(String docName, String userName, String text) {
        Document doc = new Document(docName, userName, text);
        doc.setViewerCode(generateCode("VIEW"));
        doc.setEditorCode(generateCode("EDIT"));
        doc.setText(text); // Set empty text initially

        // Save the document and CRDT trees for editing/viewing codes
        documents.put(doc.getDocID(), doc);
        crdtTrees.put(doc.getViewerCode(), new CRDTTree());
        crdtTrees.put(doc.getEditorCode(), crdtTrees.get(doc.getViewerCode()));
        sessionUsers.put(doc.getViewerCode(), new HashMap<>());
        sessionUsers.put(doc.getEditorCode(), sessionUsers.get(doc.getViewerCode()));

        // Save documents to file
        saveDocuments();
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

    public Document getDocumentByID(String docId) {
        for (Document doc : documents.values()) {
            if (doc.getDocID().equals(docId) ) {
                return doc;
            }
        }
        return null;
    }

    public CRDTTree getCRDTTree(String code) {
        if (!crdtTrees.containsKey(code)) {

            CRDTTree newTree = new CRDTTree();
            crdtTrees.put(code, newTree);
            System.out.println("Initialized new CRDTTree for code: " + code);

        }

        return crdtTrees.get(code);
    }

    public void printAllTrees() {
        for(Map.Entry<String, CRDTTree> entry : crdtTrees.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }


    public Map<String, User> getUsers(String code) {
        return sessionUsers.get(code);
    }

    public void updateDocumentText(String code, String text) {
        for (Document doc : documents.values()) {
            if (doc.getViewerCode().equals(code) || doc.getEditorCode().equals(code)) {
                doc.setText(text);
                break;
            }
        }

    }

    public void saveDocuments() {
        try {
            // Save the documents map to the file
            objectMapper.writeValue(new File(filePath), documents);
            System.out.println("Documents saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving documents: " + e.getMessage());
        }
    }

    public void loadDocuments() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                // Load the documents map from the file
                documents = objectMapper.readValue(file, objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Document.class));
                System.out.println("Documents loaded successfully.");
            } else {
                System.out.println("No existing documents found, starting fresh.");
            }
        } catch (IOException e) {
            System.err.println("Error loading documents: " + e.getMessage());
        }
    }
}
