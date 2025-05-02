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
    @SendTo("/app/session/{sessionCode}")
    public String OperationReceived(@DestinationVariable String sessionCode, @Payload Operation operation) {
            System.out.println("A7aaaaaaaaaaaaaaa");
            if(operation.getType().equals("insert")){
                service.insert(operation.getNode());
            }
            return service.getState(sessionCode);
    }






}
