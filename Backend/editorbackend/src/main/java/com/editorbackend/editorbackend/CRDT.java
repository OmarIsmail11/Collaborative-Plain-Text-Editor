package main.java.com.editorbackend.editorbackend;

import main.java.com.editorbackend.*;;  

public class CRDT {
    list<CRDTNode> head;

    public CRDT() {
        head = new list<CRDTNode>();
    }

    public void insertNode(CRDTNode node, CRDTNode parent) {
        head.add(node);
    }

    public void deleteNode(CRDTNode node) {
        node.setDeleted(true);
    }

}
