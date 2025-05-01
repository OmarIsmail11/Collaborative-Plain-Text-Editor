package com.editorbackend.Controllers;


import com.editorbackend.CRDT.CRDTTree;
import com.editorbackend.CRDTService.CRDTService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
public class webSocketController {
        private final CRDTService service;

        public webSocketController(CRDTService service) {
            this.service = service;
        }

//        @MessageMapping("/editor/{docCode}")
//        @SendTo("topic/editor/{docCode}")
//        public




}
