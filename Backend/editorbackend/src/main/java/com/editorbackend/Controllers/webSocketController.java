package com.editorbackend.Controllers;


import com.editorbackend.CRDT.Operation;
import com.editorbackend.CRDTService.CRDTService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
public class webSocketController {
        private final CRDTService service;

        public webSocketController(CRDTService service) {
            this.service = service;
        }

    @MessageMapping("/session/{sessionCode}")
    @SendTo("/topic/session/{sessionCode}")
    public String subscribeToEditor(@DestinationVariable String sessionCode, @Payload Operation operation) {
        System.out.println("SUBSCRIBED pollId: " + sessionCode);
        return service.getState(sessionCode);
    }


    @MessageMapping("/operation/{sessionCode}")
    @SendTo("/topic/session/{sessionCode}")
    public Operation broadcastReceivedOperation(@DestinationVariable String sessionCode, @Payload Operation operation) {
            System.out.println("Recieved operation from pollId: " + sessionCode);
            if(operation.getType().equals("insert")){
                service.insert(sessionCode, operation.getNode().getUserID(), operation.getNode());
            }
            if(operation.getType().equals("delete")){
                service.delete(sessionCode,operation.getNode().getUserID(),operation.getIndex());
            }
            System.out.println("Broadcasting operation received operation from pollId: " + sessionCode);
            return operation;
    }






}
