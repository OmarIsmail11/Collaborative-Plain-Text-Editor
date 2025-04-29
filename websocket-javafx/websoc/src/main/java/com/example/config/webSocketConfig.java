package com.example.config;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;



public class webSocketConfig {
    StompSession stompSession;

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
    public static void connectToWebSocket() {

        try {
            // Create a WebSocket client
            List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
            SockJsClient sockJsClient = new SockJsClient(transports);

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            List<MessageConverter> converters = new ArrayList<>();
            converters.add(new MappingJackson2MessageConverter()); // used to handle json messages
            converters.add(new StringMessageConverter()); // used to handle raw string messages
            stompClient.setMessageConverter(new CompositeMessageConverter(converters));
            // Connect to the WebSocket server
            String url = "ws://localhost:8080/ws"; // WebSocket endpoint
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
            StompSession StompSession = stompClient.connectAsync(url, sessionHandler).get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void close(){
        this.stompSession.disconnect();
    }
}