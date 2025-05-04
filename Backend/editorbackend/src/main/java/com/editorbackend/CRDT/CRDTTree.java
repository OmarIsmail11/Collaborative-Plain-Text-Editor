package com.editorbackend.CRDT;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;



public class CRDTTree {
    @JsonIgnore
    private CRDTNode root;

    @JsonIgnore
    private CRDTNode visibleRoot;

    @JsonProperty("nodeList")
    private List<CRDTNode> nodeList;

    @JsonProperty("formatter")
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @JsonIgnore
    public List<Character> visibleText = new ArrayList<>();

    @JsonIgnore
    public List<CRDTNode> visibleNodes = new ArrayList<>();

    public CRDTTree() {
        this.nodeList = new ArrayList<>();
        this.root = new CRDTNode("ROOT", '\0', LocalDateTime.now().toString(), false, null, null, -1);
        nodeList.add(root);
        this.visibleRoot = new CRDTNode("ROOT", '\0', LocalDateTime.now().toString(), false, null, null, -1);
        visibleNodes.add(visibleRoot);
    }

    public CRDTNode insert(CRDTNode node) {
        visibleText.clear();
        visibleNodes.clear();
        dfsBuildText(root);
        dfsBuildVisibleNodes(root);

        CRDTNode parent;
        int insertionIndex;

        if (node.getIndex() <= 0) {
            parent = root;
            insertionIndex = 0;
        } else if (node.getIndex() > visibleNodes.size()) {
            while (visibleNodes.size() < node.getIndex()) {
                CRDTNode lastNode = visibleNodes.isEmpty() ? root : visibleNodes.get(visibleNodes.size() - 1);
                parent = lastNode.getParent() == null ? root : lastNode.getParent();
                insertionIndex = lastNode.getIndex() + 1;

                String dummyNodeId = node.getUserID() + "_DUMMY_" + visibleNodes.size();
                CRDTNode dummyNode = new CRDTNode(dummyNodeId, ' ', LocalDateTime.now().toString(), false, parent, node.getUserID(), insertionIndex);
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
            CRDTNode targetNode = visibleNodes.get(node.getIndex() - 1);
            parent = targetNode.getParent();
            insertionIndex = targetNode.getIndex() + 1;
        }

        node.setParent(parent);
        parent.getNextNodes().add(insertionIndex, node);
        nodeList.add(node);

        for (int i = 0; i < parent.getNextNodes().size(); i++) {
            parent.getNextNodes().get(i).setIndex(i);
        }

        return node;
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

    // Rebuild tree from nodeList
    public void rebuildFromNodeList() {
        if (nodeList == null || nodeList.isEmpty()) {
            throw new IllegalStateException("nodeList is empty or null");
        }

        // Find root (node with id starting  "ROOT" and parent == null)
        root = nodeList.stream()
                .filter(node -> node.getId().startsWith("ROOT") && node.getParent() == null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Root node not found in nodeList"));

        // Find visibleRoot (node with id "ROOT" in visibleNodes or fallback to root)
        visibleRoot = nodeList.stream()
                .filter(node -> node.getId().startsWith("ROOT") && visibleNodes.contains(node))
                .findFirst()
                .orElse(root);

        // Rebuild visibleNodes and visibleText
        visibleNodes.clear();
        visibleText.clear();
        dfsBuildVisibleNodes(root);
        dfsBuildText(root);
    }

    // Getters and setters
    public CRDTNode getRoot() {
        return root;
    }

    public void setRoot(CRDTNode root) {
        this.root = root;
    }

    public CRDTNode getVisibleRoot() {
        return visibleRoot;
    }

    public void setVisibleRoot(CRDTNode visibleRoot) {
        this.visibleRoot = visibleRoot;
    }

    public List<CRDTNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<CRDTNode> nodeList) {
        this.nodeList = nodeList;
    }

    public List<Character> getVisibleText() {
        return visibleText;
    }

    public void setVisibleText(List<Character> visibleText) {
        this.visibleText = visibleText;
    }

    public List<CRDTNode> getVisibleNodes() {
        return visibleNodes;
    }

    public void setVisibleNodes(List<CRDTNode> visibleNodes) {
        this.visibleNodes = visibleNodes;
    }

    @Override
    public String toString() {
        return "CRDTTree{nodeList=" + nodeList + "}";
    }
}