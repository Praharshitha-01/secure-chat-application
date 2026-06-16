package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.demo.service.ChatService;
import com.example.demo.security.AESUtil;

import java.util.*;

@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    // 🔥 store username → session
    private static Map<String, WebSocketSession> userSessions = new HashMap<>();

    @Autowired
    private ChatService service;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        try {
            String query = session.getUri().getQuery(); // username=a
            String username = query.split("=")[1];

            session.getAttributes().put("username", username);

            userSessions.put(username, session);

            broadcastUsers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

        try {
            String msg = message.getPayload();

            if (!msg.contains("|") || !msg.contains(":")) return;

            // sender|receiver:message
            String[] first = msg.split("\\|");
            String sender = first[0];

            String[] second = first[1].split(":", 2);
            String receiver = second[0];
            String content = second[1];

            // 🔐 encrypt
            String encrypted = AESUtil.encrypt(content);

            // 💾 save to DB
            service.save(sender, receiver, encrypted);

            // 🔓 decrypt for sending
            String decrypted = AESUtil.decrypt(encrypted);

            String finalMsg = sender + ": " + decrypted;

            // 🔥 send ONLY to receiver
            WebSocketSession receiverSession = userSessions.get(receiver);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(finalMsg));
            }

            // 🔥 also send back to sender
            WebSocketSession senderSession = userSessions.get(sender);
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.sendMessage(new TextMessage(finalMsg));
            }

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void broadcastUsers() {

        try {
            String users = "USERS:" + String.join(",", userSessions.keySet());

            for (WebSocketSession s : userSessions.values()) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(users));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}