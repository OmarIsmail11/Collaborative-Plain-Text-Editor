package com.editorbackend.Controllers;


import com.editorbackend.CRDT.CRDTTree;
import com.editorbackend.CRDT.Operation;
import com.editorbackend.CRDTService.CRDTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
public class webSocketController {
    private final CRDTService service;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public webSocketController(CRDTService service, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/initialState/{sessionCode}/{userID}")
    public void sendInitialState(@DestinationVariable String sessionCode, @DestinationVariable String userID, @Payload(required = false) String payload) {
        System.out.println("Received initial state request for sessionCode: " + sessionCode + ", userID: " + userID);
        CRDTTree initialState = service.getInitialState(sessionCode);
        initialState.printCRDTTree();
        messagingTemplate.convertAndSend("/queue/initialState/" + sessionCode + "/" + userID, initialState);
        System.out.println("Sent tree");
        initialState.printCRDTTree();
        System.out.println("Sent initial state to userID: " + userID + " for session: " + sessionCode);
    }

    @MessageMapping("/operation/{sessionCode}")
    @SendTo("/topic/session/{sessionCode}")
    public Operation broadcastReceivedOperation(@DestinationVariable String sessionCode, @Payload Operation operation) {
        System.out.println("Received operation from: " + sessionCode);
        if (operation.getType().equals("insert")) {
            service.insert(sessionCode, operation.getNode().getUserID(), operation.getNode());
        }
        if (operation.getType().equals("delete")) {
            service.delete(sessionCode, operation.getNode().getUserID(), operation.getIndex());
        }
        System.out.println("Broadcasting operation for: " + sessionCode);
        operation.getNode().printNode();
        return operation;
    }
}
