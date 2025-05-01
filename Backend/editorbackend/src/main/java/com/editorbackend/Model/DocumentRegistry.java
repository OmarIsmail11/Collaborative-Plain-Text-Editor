package com.editorbackend.Model;

import com.editorbackend.CRDT.CRDTTree;
import com.editorbackend.CRDT.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
public class DocumentRegistry {
    private Map<String, Document> documents = new HashMap<>();
    private Map<String, CRDTTree> crdtTrees = new HashMap<>();
    private Map<String, Map<String, User>> sessionUsers = new HashMap<>();

    public Document createDocument(String docName) {
        Document doc = new Document(docName);
        doc.setViewerCode(generateCode("VIEW"));
        doc.setEditorCode(generateCode("EDIT"));
        doc.setText("");

        documents.put(doc.getDocName(), doc);
        crdtTrees.put(doc.getViewerCode(), new CRDTTree());
        crdtTrees.put(doc.getEditorCode(), crdtTrees.get(doc.getViewerCode()));
        sessionUsers.put(doc.getViewerCode(), new HashMap<>());
        sessionUsers.put(doc.getEditorCode(), sessionUsers.get(doc.getViewerCode()));
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

    public CRDTTree getCRDTTree(String code) {
        return crdtTrees.get(code);
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
}