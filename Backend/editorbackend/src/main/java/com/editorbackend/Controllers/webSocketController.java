package com.editorbackend.Controllers;


import com.editorbackend.CRDT.CRDTTree;
import org.springframework.stereotype.Controller;


@Controller
public class webSocketController {
        private CRDTTree crdt;

        public webSocketController(CRDTTree crdt) {
            this.crdt = crdt;
        }




}
