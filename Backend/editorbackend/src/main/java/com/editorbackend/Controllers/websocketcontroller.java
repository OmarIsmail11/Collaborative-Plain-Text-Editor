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
import com.editorbackend.CRDT.User;

import java.util.*;


@Controller
public class websocketcontroller {
    private final CRDTService service;
    private final SimpMessagingTemplate messagingTemplate;
    public Map<String,User> Users;

    @Autowired
    public websocketcontroller(CRDTService service, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
        this.Users = new Map<String, User>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public User get(Object key) {
                return null;
            }

            @Override
            public User put(String key, User value) {
                return null;
            }

            @Override
            public User remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ? extends User> m) {

            }

            @Override
            public void clear() {

            }

            @Override
            public Set<String> keySet() {
                return Set.of();
            }

            @Override
            public Collection<User> values() {
                return List.of();
            }

            @Override
            public Set<Entry<String, User>> entrySet() {
                return Set.of();
            }
        };

    }

    @MessageMapping("/initialState/{sessionCode}/{userID}")
    @SendTo("/initialState/{sessionCode}")
    public void sendInitialState(@DestinationVariable String sessionCode, @DestinationVariable String userID, @Payload(required = false) String payload) {
        System.out.println("Received initial state request for sessionCode: " + sessionCode + ", userID: " + userID);
        CRDTTree initialState = service.getInitialState(sessionCode);
        initialState.printCRDTTree();
        messagingTemplate.convertAndSend("/queue/initialState/" + sessionCode + "/" + userID, initialState);
        System.out.println("Sent tree");
        initialState.printCRDTTree();
        System.out.println("Sent initial state to userID: " + userID + " for session: " + sessionCode);
        User newuser = new User(userID);
        Users.putIfAbsent(sessionCode, newuser);
        System.out.println("Sent new user: " + newuser.getUserID());
        for (User user : Users.values()) {
            messagingTemplate.convertAndSend("/topic/User/" + sessionCode, user);
        }
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
        if(operation.getType().equals("undo")){
            service.undo(sessionCode, operation.getNode().getUserID());
        }
        if(operation.getType().equals("redo")){
            service.redo(sessionCode, operation.getNode().getUserID());
        }
        System.out.println("Broadcasting operation for: " + sessionCode);
        operation.getNode().printNode();
        return operation;
    }

    @MessageMapping("/undo/{sessionCode}")
    @SendTo("/topic/undo/{sessionCode}")
    public Operation undo(@DestinationVariable String sessionCode, @Payload Operation operation) {
        System.out.println("Received undo operation from: " + sessionCode);
        operation.getNode().printNode();
        service.undo(sessionCode, operation.getNode().getUserID());
        return operation;
    }
    }

