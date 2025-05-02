package com.editorbackend.CRDTService;

import com.editorbackend.CRDT.CRDTNode;
import com.editorbackend.CRDT.User;
import com.editorbackend.Model.Document;
import com.editorbackend.Model.DocumentRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.editorbackend.CRDT.*;


@Service
public class CRDTService {
    @Autowired
    private DocumentRegistry documentRegistry;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public CRDTNode insert(String code, String userId,CRDTNode newNode) {
        CRDTTree tree = documentRegistry.getCRDTTree(code);
        Map<String, User> users = documentRegistry.getUsers(code);
        if (tree == null) return null;

        User user = users.computeIfAbsent(userId, k -> new User(userId));
        CRDTNode node = tree.insert(value, index, timestamp, userId);
        CRDTNode node = tree.insert(newNode)
        user.addToUndoStack("insert", node, index);


        StringBuilder sb = new StringBuilder();
        for (char c : tree.visibleText) {
            sb.append(c);
        }
        documentRegistry.updateDocumentText(code, sb.toString());



        return node;
    }

    public void delete(String code, String userId, int index) {
        CRDTTree tree = documentRegistry.getCRDTTree(code);
        Map<String, User> users = documentRegistry.getUsers(code);
        if (tree == null) return;

        User user = users.computeIfAbsent(userId, k -> new User(userId));
        List<CRDTNode> visibleNodes = tree.visibleNodes;
        if (index >= 0 && index < visibleNodes.size()) {
            CRDTNode node = visibleNodes.get(index);
            tree.delete(index, userId);
            user.addToUndoStack("delete", node, index);

            // Update Document text
            StringBuilder sb = new StringBuilder();
            for (char c : tree.visibleText) {
                sb.append(c);
            }
            documentRegistry.updateDocumentText(code, sb.toString());

            tree.printText();
            tree.printCRDTTree();
        }
    }

    public Operation undo(String code, String userId) {
        Map<String, User> users = documentRegistry.getUsers(code);
        CRDTTree tree = documentRegistry.getCRDTTree(code);
        if (users == null || tree == null) return null;

        User user = users.get(userId);
        if (user == null) return null;

        Operation op = user.undo(tree);
        if (op != null) {
            // Update Document text
            StringBuilder sb = new StringBuilder();
            for (char c : tree.visibleText) {
                sb.append(c);
            }
            documentRegistry.updateDocumentText(code, sb.toString());


        }
        return op;
    }

    public Operation redo(String code, String userId) {
        Map<String, User> users = documentRegistry.getUsers(code);
        CRDTTree tree = documentRegistry.getCRDTTree(code);
        if (users == null || tree == null) return null;

        User user = users.get(userId);
        if (user == null) return null;

        Operation op = user.redo(tree);
        if (op != null) {
            // Update Document text
            StringBuilder sb = new StringBuilder();
            for (char c : tree.visibleText) {
                sb.append(c);
            }
            documentRegistry.updateDocumentText(code, sb.toString());

        }
        return op;
    }

    public String getState(String code) {
        CRDTTree tree = documentRegistry.getCRDTTree(code);
        if (tree == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : tree.visibleText) {
            sb.append(c);
        }
        return sb.toString();
    }

//    public void updateDocumentText(Document doc, String newText) {
//        Document document = documentRegistry.getDocumentByCode();
//
//
//
//
//    }
}