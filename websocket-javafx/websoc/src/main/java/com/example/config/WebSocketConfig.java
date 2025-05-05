package com.example.config;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.example.Controllers.PrimaryController;
import com.example.Model.CRDTNode;
import com.example.Model.CRDTTree;
import com.example.Model.Operation;
import com.example.Model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;



@Component
public class WebSocketConfig {
    private StompSession stompSession;
    private Consumer<String> textUpdateCallback;
    private Consumer<CRDTTree> crdtTreeInitializer;
    private WebSocketStompClient stompClient;
    private Consumer<Operation> operationHandler;
    private Consumer<Operation> undoHandler;
    private String sessionCode;
    private final Object crdtLock = new Object();
    private Consumer<User> userManagement;

    // Remove @Autowired and the primaryController field
    // private PrimaryController primaryController;

    public WebSocketConfig() {
    }

    public void setOperationHandler(Consumer<Operation> handler) {
        this.operationHandler = handler;
    }
    public void setUndoHandler(Consumer<Operation> handler) {
        this.undoHandler = handler;
    }

    public void setUserManagement(Consumer<User> handler) {
        this.userManagement = handler;
    }
    public void setTextUpdateCallback(Consumer<String> callback) {
        this.textUpdateCallback = callback;
    }

    public void setCRDTTreeInitializer(Consumer<CRDTTree> initializer) {
        this.crdtTreeInitializer = initializer;
    }

    public void sendOperation(String sessionCode, Operation operation) {
        if (stompSession != null && stompSession.isConnected()) {
            String destination = "/app/operation/" + sessionCode;
            stompSession.send(destination, operation);
            System.out.println("Operation sent to: " + destination);
        } else {
            System.err.println("Cannot send operation: session is null or disconnected");
        }

    }

    public void requestInitialState(String sessionCode,String UserId) {
        if (stompSession != null && stompSession.isConnected()) {
            String destination = "/app/initialState/" + sessionCode+"/"+UserId;
            stompSession.send(destination, ""); // Empty message as request
            System.out.println("Requested initial state for session: " + sessionCode);
        } else {
            System.err.println("Cannot request initial state: session is null or disconnected");
        }
    }

    public void getUserList(String sessionCode) {
        if (stompSession != null && stompSession.isConnected()) {
            String topic = "/topic/User/" + sessionCode;
            System.out.println("Subscribing to: " + topic + ", session connected: " + stompSession.isConnected());
            stompSession.subscribe(topic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return User.class;
                }

                public void handleFrame(StompHeaders headers, Object payload) {
                    if (payload instanceof User)
                    {
                        User newUser = (User) payload;
                        if (userManagement != null) {
                            userManagement.accept(newUser);
                            System.out.println("Accepted user for session: " + sessionCode);
                        }
                    }

                }
            });
        }
    }

    // 2. Subscribe to initial state topic
    public void subscribeToInitialState(String sessionCode, String UserID) {
        if (stompSession != null && stompSession.isConnected()) {
            String topic = "/queue/initialState/" + sessionCode + "/" + UserID;
            System.out.println("Subscribing to: " + topic + ", session connected: " + stompSession.isConnected());
            stompSession.subscribe(topic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return CRDTTree.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("Received payload: " + payload);
                    if (payload instanceof CRDTTree) {
                        CRDTTree initialState = (CRDTTree) payload;
                        // Rebuild the tree from nodeList
                        initialState.rebuildFromNodeList();
                        if (crdtTreeInitializer != null) {
                            crdtTreeInitializer.accept(initialState);
                        }
                        if (textUpdateCallback != null) {
                            StringBuilder content = new StringBuilder();
                            synchronized (crdtLock) {
                                List<CRDTNode> nodes = initialState.getVisibleNodes();
                                if (nodes != null) {
                                    for (CRDTNode node : nodes) {
                                        content.append(node.getValue());
                                    }
                                }
                                textUpdateCallback.accept(content.toString());
                            }
                        }
                    }
                    else {
                        System.err.println("Unexpected payload type: " + (payload != null ? payload.getClass().getName() : "null"));
                    }
                }
            });
            System.out.println("Subscribed to initial state for session: " + sessionCode + ", userID: " + UserID);
        } else {
            System.err.println("Cannot subscribe to initial state: session is null or disconnected");
        }
    }
    public void connect(String sessionCode) {
        this.sessionCode = sessionCode; // Store session code for reconnection
        connectToWebSocket();
    }

    private void connectToWebSocket() {
        try {
            List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            List<MessageConverter> converters = new ArrayList<>();

            MappingJackson2MessageConverter jacksonConverter = new MappingJackson2MessageConverter();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            jacksonConverter.setObjectMapper(objectMapper);

            converters.add(jacksonConverter);
            converters.add(new StringMessageConverter());
            stompClient.setMessageConverter(new CompositeMessageConverter(converters));

            String url = "ws://localhost:8080/ws";
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
            this.stompClient = stompClient;
            stompSession = stompClient.connectAsync(url, sessionHandler).get();

            String topic = "/topic/session/" + sessionCode;
            String undoTopic = "/topic/undo/" + sessionCode;
            // /topic server ---->>> subscribers
            // /app  client -->>> server
            // /queue/topic -->>>> 1 subscriber
            stompSession.subscribe(topic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Operation.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    if (payload instanceof Operation) {
                        Operation operation = (Operation) payload;
                        System.out.println("Received operation from server: " + operation.getType());
                        if (operationHandler != null) {
                            operation.getNode().printNode();
                            operationHandler.accept(operation);
                        } else {
                            System.err.println("Operation handler is null");
                        }
                    } else {
                        System.err.println("Unexpected payload type: " + payload.getClass().getName());
                    }
                }
            });

            stompSession.subscribe(undoTopic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Operation.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    if (payload instanceof Operation) {
                        Operation operation = (Operation) payload;
                        System.out.println("Received undo from server: " + operation.getType());
                        if (undoHandler != null) {
                            if(operation.getNode() != null){
                                operation.getNode().printNode();
                                System.out.println("A7AAAAAAAAAAAAAA");
                                undoHandler.accept(operation);
                            }

                        } else {
                            System.err.println("Operation handler is null");
                        }
                    } else {
                        System.err.println("Unexpected payload type: " + payload.getClass().getName());
                    }
                    System.out.println("Subscribed to undo channel: " + sessionCode);
                }
            });


        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // Schedule reconnection attempt
            System.out.println("Connection failed, scheduling reconnect attempt");
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait 5 seconds before reconnecting
                System.out.println("Attempting to reconnect...");
                connectToWebSocket();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void sendUndo(Operation op) {
        if (stompSession != null && stompSession.isConnected()) {
            String destination = "/app/undo/" + sessionCode;
            stompSession.send(destination, op);
            System.out.println("Operation sent to: " + destination);
        } else {
            System.err.println("Cannot send operation: session is null or disconnected");
        }
    }



    // 7. Add a method in StompSessionHandler to handle disconnection
    private class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket server!");
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.err.println("An error occurred: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.err.println("Transport error: " + exception.getMessage());
            if (!session.isConnected()) {
                System.out.println("Session disconnected, attempting to reconnect");
                scheduleReconnect();
            }
        }
    }
}