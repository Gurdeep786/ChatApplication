package com.practice.chat_service.handler;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.practice.chat_service.model.ChatMessage;
import com.practice.chat_service.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private final ChatService chatService;

    public ChatHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    // 🔥 username → active sessions
    private static final Map<String, Set<WebSocketSession>> userSessions =
            new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username =
                (String) session.getAttributes().get("username");

        userSessions
                .computeIfAbsent(username, k -> new CopyOnWriteArraySet<>())
                .add(session);

        System.out.println("🟢 USER CONNECTED: " + username);
        System.out.println("📡 ACTIVE USER SESSIONS:");

        userSessions.forEach((user, sessions) -> {
            System.out.println(
                    "   - " + user + " → " + sessions.size() + " session(s)"
            );
        });
    }


    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        ChatMessage chatMessage =
                mapper.readValue(message.getPayload(), ChatMessage.class);
System.out.println(chatMessage);
// 1️⃣ Save message ALWAYS
        String sender =
                (String) session.getAttributes().get("username");


        String receiver = chatMessage.getReceiver();
        String content  = chatMessage.getContent();
        System.out.println(sender);
        // 1️⃣ Save message
        chatService.saveMessage(sender, receiver, content);

// 2️⃣ Send to receiver if online
        Set<WebSocketSession> receiverSessions =
                userSessions.get(chatMessage.getReceiver());

        if (receiverSessions != null) {
            for (WebSocketSession s : receiverSessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(message.getPayload()));
                }
            }
        }


    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        String username =
                (String) session.getAttributes().get("username");

        Set<WebSocketSession> sessions = userSessions.get(username);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(username);
            }
        }

        System.out.println(username + " disconnected");
    }
}
