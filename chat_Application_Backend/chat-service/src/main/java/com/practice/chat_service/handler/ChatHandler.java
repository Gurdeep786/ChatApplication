package com.practice.chat_service.handler;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.practice.chat_service.model.ChatMessage;
import com.practice.chat_service.service.ChatService;
import com.practice.chat_service.service.RedisPresenceService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private final ChatService chatService;
    private final RedisPresenceService presenceService;
    private final WebClient webClient;

    public ChatHandler(ChatService chatService,
                       RedisPresenceService presenceService,
                       WebClient.Builder builder) {

        this.chatService = chatService;
        this.presenceService = presenceService;
        this.webClient = builder.baseUrl("http://USER-SERVICE").build();
    }

    // 🔥 username → active sessions
    private static final Map<String, Set<WebSocketSession>> userSessions =
            new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();




    private void broadcastPresence(String username, boolean online) {

        try {

            // 1️⃣ Call user-service and get raw JSON
            String response = webClient
                    .get()
                    .uri("/user/friends/listName/" + username)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) return;

            // 2️⃣ Convert JSON string to JsonNode
            JsonNode root = mapper.readTree(response);

            String message = """
        {
          "type": "PRESENCE",
          "username": "%s",
          "online": %s
        }
        """.formatted(username, online);

            // 3️⃣ Loop through friend list
            for (JsonNode friend : root) {

                String friendName = friend.get("name").asText();

                Set<WebSocketSession> sessions = userSessions.get(friendName);

                if (sessions != null) {
                    for (WebSocketSession session : sessions) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(message));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username =
                (String) session.getAttributes().get("username");

        userSessions
                .computeIfAbsent(username, k -> new CopyOnWriteArraySet<>())
                .add(session);
        presenceService.setUserOnline(username);
        broadcastPresence(username, true);
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
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("sender", sender);
        messagePayload.put("receiver", receiver);
        messagePayload.put("content", content);
//        messagePayload.put("timestamp", LocalDateTime.now());

        String json = mapper.writeValueAsString(messagePayload);
// 2️⃣ Send to receiver if online
        Set<WebSocketSession> receiverSessions =
                userSessions.get(chatMessage.getReceiver());

        if (receiverSessions != null) {
            for (WebSocketSession s : receiverSessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
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
        presenceService.setUserOffline(username);
        broadcastPresence(username, false);
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
