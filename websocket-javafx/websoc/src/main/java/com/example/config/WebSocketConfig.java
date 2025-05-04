package com.example.config;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.example.Controllers.PrimaryController;
import com.example.Model.Operation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private WebSocketStompClient stompClient;

    private Consumer<Operation> operationHandler;

    private String sessionCode;


    @Autowired
    public PrimaryController primaryController;

    public WebSocketConfig() {

    }


    public void setOperationHandler(Consumer<Operation> handler) {
        this.operationHandler = handler;
    }


    public void setTextUpdateCallback(Consumer<String> callback) {
        this.textUpdateCallback = callback;
    }

    public void sendOperation(String sessionCode, Operation operation) {
        if (stompSession != null && stompSession.isConnected()) {
            String destination = "/app/operation/" + sessionCode; // Changed to /operation
            stompSession.send(destination, operation);
            System.out.println("Operation sent to: " + destination);
        } else {
            System.err.println("Cannot send operation: session is null or disconnected");
        }
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket server!");
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.err.println("An error occurred: " + exception.getMessage());
        }
    }

    public void connect(String sessionCode) {
        try {
            // Create a WebSocket client
            List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            List<MessageConverter> converters = new ArrayList<>();

            // Configure MappingJackson2MessageConverter with JavaTimeModule
            MappingJackson2MessageConverter jacksonConverter = new MappingJackson2MessageConverter();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // Register JavaTimeModule
            // Optional: Configure serialization of LocalDateTime (e.g., as ISO string)
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            jacksonConverter.setObjectMapper(objectMapper);

            converters.add(jacksonConverter);
            converters.add(new StringMessageConverter());
            stompClient.setMessageConverter(new CompositeMessageConverter(converters));

            // Rest of the connect method remains unchanged
            String url = "ws://localhost:8080/ws";
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
            stompSession = stompClient.connectAsync(url, sessionHandler).get();

            String topic = "/topic/session/" + sessionCode;
            stompSession.subscribe(topic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Operation.class;
                }


                @Override
                public void handleFrame(StompHeaders headers, Object payload) {


                    Operation operation = (Operation) payload;
                    System.out.println("Received operation from server: " + operation.getType());
                    primaryController.handleRemoteOperation(operation);

                }
            });

            System.out.println("Subscribed to session: " + sessionCode);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void close() {
        if (stompSession != null) {
            stompSession.disconnect();
            System.out.println("WebSocket connection closed.");
        }
    }
}