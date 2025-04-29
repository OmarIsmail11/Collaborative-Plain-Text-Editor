package com.editorbackend.CRDT;


import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class CRDTTree {
    private CRDTNode root;
    private CRDTNode visibleRoot;
    private List<CRDTNode> nodeList;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private List<Character> visibleText = new ArrayList<>();
    public List<CRDTNode> visibleNodes = new ArrayList<>();

    public CRDTTree() {
        this.nodeList = new ArrayList<>();
        this.root = new CRDTNode("ROOT", '\0', LocalDateTime.now(), false, null, null, -1);
        nodeList.add(root);
        this.visibleRoot = new CRDTNode("ROOT", '\0', LocalDateTime.now(), false, null, null, -1);
        visibleNodes.add(visibleRoot);
    }

    public CRDTNode insert(char value, int index, LocalDateTime timestamp, String userID) {
        visibleText.clear();
        visibleNodes.clear();
        dfsBuildText(root);
        dfsBuildVisibleNodes(root);
// 
        CRDTNode parent;
        int insertionIndex;

        if (index <= 0) {
            parent = root;
            insertionIndex = 0;
        } else if (index > visibleNodes.size()) {
            while (visibleNodes.size() < index) {
                CRDTNode lastNode = visibleNodes.isEmpty() ? root : visibleNodes.get(visibleNodes.size() - 1);
                parent = lastNode.getParent() == null ? root : lastNode.getParent();
                insertionIndex = lastNode.getIndex() + 1;

                String dummyNodeId = userID + "_DUMMY_" + visibleNodes.size();
                CRDTNode dummyNode = new CRDTNode(dummyNodeId, ' ', LocalDateTime.now(), false, parent, userID, insertionIndex);
                parent.getNextNodes().add(dummyNode);
                nodeList.add(dummyNode);
                visibleNodes.add(dummyNode);

                for (int i = 0; i < parent.getNextNodes().size(); i++) {
                    parent.getNextNodes().get(i).setIndex(i);
                }
            }
            parent = visibleNodes.get(visibleNodes.size() - 1).getParent();
            insertionIndex = visibleNodes.get(visibleNodes.size() - 1).getIndex() + 1;
        } else {
            CRDTNode targetNode = visibleNodes.get(index - 1);
            parent = targetNode.getParent();
            insertionIndex = targetNode.getIndex() + 1;
        }

        String formattedTimestamp = timestamp.format(formatter);
        String nodeId = userID + "_" + formattedTimestamp;

        CRDTNode newNode = new CRDTNode(nodeId, value, timestamp, false, parent, userID, insertionIndex);
        parent.getNextNodes().add(insertionIndex, newNode);
        nodeList.add(newNode);

        for (int i = 0; i < parent.getNextNodes().size(); i++) {
            parent.getNextNodes().get(i).setIndex(i);
        }

        return newNode;
    }

    public void delete(int index, String userID) {
        if (index < 0 || index >= visibleNodes.size()) {
            System.out.println("Invalid index for deletion.");
            return;
        }

        CRDTNode node = visibleNodes.get(index);
        node.setDeleted(true);
        visibleNodes.remove(index);
    }

    private void dfsBuildText(CRDTNode node) {
        List<CRDTNode> children = new ArrayList<>(node.getNextNodes());
        children.sort(Comparator.comparingInt(CRDTNode::getIndex));
        for (CRDTNode child : children) {
            if (!child.isDeleted()) {
                visibleText.add(child.getValue());
            }
            dfsBuildText(child);
        }
    }

    private void dfsBuildVisibleNodes(CRDTNode node) {
        List<CRDTNode> children = new ArrayList<>(node.getNextNodes());
        children.sort(Comparator.comparingInt(CRDTNode::getIndex));
        for (CRDTNode child : children) {
            if (!child.isDeleted()) {
                visibleNodes.add(child);
            }
            dfsBuildVisibleNodes(child);
        }
    }

    public void printText() {
        visibleText.clear();
        visibleNodes.clear();
        dfsBuildText(root);
        dfsBuildVisibleNodes(root);

        System.out.println("Reconstructed Text:");
        StringBuilder sb = new StringBuilder();
        for (char c : visibleText) {
            sb.append(c);
        }
        System.out.println(sb.toString());
    }

    private void printTree(CRDTNode node, String prefix, boolean isLast) {
        if (node != root) {
            System.out.println(prefix + (isLast ? "└── " : "├── ") +
                (node.isDeleted() ? "(" + node.getValue() + ", idx=" + node.getIndex() + ", " + node.getTimestamp() + ")"
                                  : node.getValue() + ", idx=" + node.getIndex() + ", " + node.getTimestamp()));
            prefix += isLast ? "    " : "│   ";
        }
        List<CRDTNode> children = node.getNextNodes();
        for (int i = 0; i < children.size(); i++) {
            printTree(children.get(i), prefix, i == children.size() - 1);
        }
    }

    public void printCRDTTree() {
        System.out.println("CRDT Tree Structure:");
        printTree(root, "", true);
    }
}
