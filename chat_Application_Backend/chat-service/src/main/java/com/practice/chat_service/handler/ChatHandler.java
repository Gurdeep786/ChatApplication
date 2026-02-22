//package com.practice.chat_service.handler;
//
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import com.practice.chat_service.model.ChatMessage;
//import com.practice.chat_service.service.ChatService;
//import com.practice.chat_service.service.RedisPresenceService;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//@Component
//public class ChatHandler extends TextWebSocketHandler {
//    private final ChatService chatService;
//    private final RedisPresenceService presenceService;
//    private final WebClient webClient;
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final ChannelTopic topic;
//    public ChatHandler(ChatService chatService,
//                       RedisPresenceService presenceService,
//                       WebClient.Builder builder,
//     RedisTemplate<String, Object>redisTemplate, // Added
//                       ChannelTopic topic) {
//
//        this.chatService = chatService;
//        this.presenceService = presenceService;
//        this.webClient = builder.baseUrl("http://USER-SERVICE").build();
//        this.redisTemplate = redisTemplate;
//        this.topic = topic;
//    }
//
//    // 🔥 username → active sessions
//    private static final Map<String, Set<WebSocketSession>> userSessions =
//            new ConcurrentHashMap<>();
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//
//
//
//    public void broadcastPresence(String username, boolean online) {
//
//        try {
//
//            // 1️⃣ Call user-service and get raw JSON
//            String response = webClient
//                    .get()
//                    .uri("/user/friends/listName/" + username)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//
//            if (response == null) return;
//
//            // 2️⃣ Convert JSON string to JsonNode
//            JsonNode root = mapper.readTree(response);
//
//            String message = """
//        {
//          "type": "PRESENCE",
//          "username": "%s",
//          "online": %s
//        }
//        """.formatted(username, online);
//
//            // 3️⃣ Loop through friend list
//            for (JsonNode friend : root) {
//
//                String friendName = friend.get("name").asText();
//
//                Set<WebSocketSession> sessions = userSessions.get(friendName);
//
//                if (sessions != null) {
//                    for (WebSocketSession session : sessions) {
//                        if (session.isOpen()) {
//                            session.sendMessage(new TextMessage(message));
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void syncFriendStatus(WebSocketSession session, String username) {
//        try {
//            // Fetch friend list for the person who just logged in
//            String response = webClient
//                    .get()
//                    .uri("/user/friends/listName/" + username)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
//
//            if (response == null) return;
//            JsonNode root = mapper.readTree(response);
//
//            for (JsonNode friend : root) {
//                String friendName = friend.get("name").asText();
//
//                // Check if this friend has any active sessions
//                boolean isFriendOnline = presenceService.isUserOnline(friendName);
//
//                if (isFriendOnline) {
//                    String message = """
//                {
//                  "type": "PRESENCE",
//                  "username": "%s",
//                  "online": true
//                }
//                """.formatted(friendName);
//
//                    session.sendMessage(new TextMessage(message));
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error syncing initial presence: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        String username =
//                (String) session.getAttributes().get("username");
//
//        userSessions
//                .computeIfAbsent(username, k -> new CopyOnWriteArraySet<>())
//                .add(session);
//        presenceService.setUserOnline(username);
//        broadcastPresence(username, true);
//        syncFriendStatus(session, username);
//        System.out.println("🟢 USER CONNECTED: " + username);
//        System.out.println("📡 ACTIVE USER SESSIONS:");
//
//        userSessions.forEach((user, sessions) -> {
//            System.out.println(
//                    "   - " + user + " → " + sessions.size() + " session(s)"
//            );
//        });
//    }
//
//
//    @Override
//    protected void handleTextMessage(
//            WebSocketSession session,
//            TextMessage message) throws Exception {
//
//        ChatMessage chatMessage =
//                mapper.readValue(message.getPayload(), ChatMessage.class);
//System.out.println(chatMessage);
//// 1️⃣ Save message ALWAYS
//        String sender =
//                (String) session.getAttributes().get("username");
//        if ("HEARTBEAT".equalsIgnoreCase(chatMessage.getType())) {
//            System.out.println("💓 Heartbeat received from: " + sender);
//            boolean wasOnline = presenceService.isUserOnline(sender);
//            presenceService.refreshUserOnline(sender);
//            if (!wasOnline) {
//                System.out.println("🟢 User back online via heartbeat: " + sender);
//                broadcastPresence(sender, true);
//            }
//            return;
//        }
//
//        String receiver = chatMessage.getReceiver();
//        String content  = chatMessage.getContent();
//        System.out.println(sender);
//        // 1️⃣ Save message
//        chatService.saveMessage(sender, receiver, content);
//        ObjectMapper mapper = new ObjectMapper();
//
//        Map<String, Object> messagePayload = new HashMap<>();
//        messagePayload.put("sender", sender);
//        messagePayload.put("receiver", receiver);
//        messagePayload.put("content", content);
////        messagePayload.put("timestamp", LocalDateTime.now());
//
//        String json = mapper.writeValueAsString(messagePayload);
//// 2️⃣ Send to receiver if online
////        Set<WebSocketSession> receiverSessions =
////                userSessions.get(chatMessage.getReceiver());
////
////        if (receiverSessions != null) {
////            for (WebSocketSession s : receiverSessions) {
////                if (s.isOpen()) {
////                    s.sendMessage(new TextMessage(json));
////                }
////            }
////        }
//        redisTemplate.convertAndSend(topic.getTopic(), json);
//
//    }
//    public void handleRedisMessage(String message) {
//        try {
//            System.out.println("message ="+message);
//            JsonNode jsonNode = mapper.readTree(message);
//            String receiver = jsonNode.get("receiver").asText();
//            System.out.println("receiver ="+receiver);
//            // Check if the receiver is connected to THIS specific server instance
//            Set<WebSocketSession> localSessions = userSessions.get(receiver);
//            if (localSessions != null) {
//                for (WebSocketSession s : localSessions) {
//                    if (s.isOpen()) {
//                        s.sendMessage(new TextMessage(message));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error handling Redis message: " + e.getMessage());
//        }
//    }
//    @Override
//    public void afterConnectionClosed(
//            WebSocketSession session,
//            CloseStatus status) {
//
//        String username =
//                (String) session.getAttributes().get("username");
//
//
//        Set<WebSocketSession> sessions = userSessions.get(username);
//        if (sessions != null) {
//            sessions.remove(session);
//            if (sessions.isEmpty()) {
//                userSessions.remove(username);
//                broadcastPresence(username, false);
//                presenceService.setUserOffline(username);
//            }
//        }
//
//        System.out.println(username + " disconnected");
//    }
//}




package com.practice.chat_service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.chat_service.model.ChatMessage;
import com.practice.chat_service.service.ChatService;
import com.practice.chat_service.service.RedisPresenceService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final RedisPresenceService presenceService;
    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ChannelTopic chatTopic;
    private final ChannelTopic presenceTopic;

    public ChatHandler(ChatService chatService,
                       RedisPresenceService presenceService,
                       WebClient.Builder builder,
                       RedisTemplate<String, Object> redisTemplate,
                       ChannelTopic chatTopic,
                       ChannelTopic presenceTopic) {

        this.chatService = chatService;
        this.presenceService = presenceService;
        this.webClient = builder.baseUrl("http://USER-SERVICE").build();
        this.redisTemplate = redisTemplate;
        this.chatTopic = chatTopic;
        this.presenceTopic = presenceTopic;
    }

    // 🔥 username → active sessions (local instance only)
    private static final Map<String, Set<WebSocketSession>> userSessions =
            new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    /* ============================================================
       🟢 WebSocket CONNECT
       ============================================================ */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String username =
                (String) session.getAttributes().get("username");

        System.out.println("\n==========================================");
        System.out.println("🟢 CONNECT EVENT");
        System.out.println("User: " + username);

        userSessions
                .computeIfAbsent(username, k -> new CopyOnWriteArraySet<>())
                .add(session);

        System.out.println("📡 Active Sessions Map: " + userSessions.keySet());

        presenceService.setUserOnline(username);
        printTTL(username, "CREATED");

        publishPresence(username, true);

        syncFriendStatus(session, username);

        System.out.println("==========================================\n");
    }

    /* ============================================================
       💬 Handle WebSocket Messages
       ============================================================ */
    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        ChatMessage chatMessage =
                mapper.readValue(message.getPayload(), ChatMessage.class);

        String sender =
                (String) session.getAttributes().get("username");

        // 💓 HEARTBEAT
        if ("HEARTBEAT".equalsIgnoreCase(chatMessage.getType())) {

            System.out.println("\n💓 HEARTBEAT RECEIVED → " + sender);

            boolean wasOnline = presenceService.isUserOnline(sender);

            presenceService.refreshUserOnline(sender);

            printTTL(sender, "REFRESHED");

            if (!wasOnline) {
                System.out.println("⚠ User was offline in Redis → Publishing ONLINE again");
                publishPresence(sender, true);
            }

            return;
        }

        // 💬 CHAT MESSAGE
        System.out.println("\n💬 CHAT MESSAGE");
        System.out.println("Sender: " + sender);
        System.out.println("Receiver: " + chatMessage.getReceiver());
        System.out.println("Content: " + chatMessage.getContent());

        chatService.saveMessage(sender,
                chatMessage.getReceiver(),
                chatMessage.getContent());

        String json = mapper.writeValueAsString(chatMessage);

        redisTemplate.convertAndSend(chatTopic.getTopic(), json);

        System.out.println("📢 Chat published to Redis channel\n");
    }

    /* ============================================================
       📩 Receive Chat Message From Redis
       ============================================================ */
    public void handleRedisMessage(String message) {

        try {
            System.out.println("\n📩 CHAT RECEIVED FROM REDIS");
            System.out.println("Payload: " + message);

            JsonNode jsonNode = mapper.readTree(message);
            String receiver = jsonNode.get("receiver").asText();

            Set<WebSocketSession> localSessions = userSessions.get(receiver);

            if (localSessions != null) {
                System.out.println("📡 Delivering to local sessions of: " + receiver);

                for (WebSocketSession s : localSessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(message));
                    }
                }
            } else {
                System.out.println("⚠ No local session for: " + receiver);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ============================================================
       📢 Receive Presence Event From Redis
       ============================================================ */
    public void handlePresenceEvent(String message) {

        try {
            System.out.println("\n📢 PRESENCE EVENT RECEIVED FROM REDIS");
            System.out.println("Payload: " + message);

            JsonNode jsonNode = mapper.readTree(message);

            String username = jsonNode.get("username").asText();
            boolean online = jsonNode.get("online").asBoolean();

            broadcastPresence(username, online);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ============================================================
       🔴 WebSocket CLOSE
       ============================================================ */
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        String username =
                (String) session.getAttributes().get("username");

        System.out.println("\n==========================================");
        System.out.println("🔴 DISCONNECT EVENT");
        System.out.println("User: " + username);

        Set<WebSocketSession> sessions = userSessions.get(username);

        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                userSessions.remove(username);

                presenceService.setUserOffline(username);

                publishPresence(username, false);

                System.out.println("📡 User removed from active sessions");
            }
        }

        System.out.println("==========================================\n");
    }

    /* ============================================================
       📢 Publish Presence Event
       ============================================================ */
    public void publishPresence(String username, boolean online) {

        try {
            Map<String, Object> payload = Map.of(
                    "type", "PRESENCE",
                    "username", username,
                    "online", online
            );

            String json = mapper.writeValueAsString(payload);

            redisTemplate.convertAndSend(
                    presenceTopic.getTopic(),
                    json
            );

            System.out.println("📢 PRESENCE PUBLISHED → "
                    + username + " : " + (online ? "ONLINE" : "OFFLINE"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ============================================================
       📡 Broadcast To Local Friend Sessions
       ============================================================ */
    private void broadcastPresence(String username, boolean online) {

        try {
            System.out.println("📡 Broadcasting presence of "
                    + username + " → " + (online ? "ONLINE" : "OFFLINE"));

            String response = webClient
                    .get()
                    .uri("/user/friends/listName/" + username)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) return;

            JsonNode root = mapper.readTree(response);

            for (JsonNode friend : root) {

                String friendName = friend.get("name").asText();

                Set<WebSocketSession> sessions = userSessions.get(friendName);

                if (sessions != null) {
                    System.out.println("   ➜ Notifying friend: " + friendName);

                    for (WebSocketSession session : sessions) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(
                                    "{\"type\":\"PRESENCE\",\"username\":\""
                                            + username + "\",\"online\":" + online + "}"
                            ));
                        }
                    }
                } else {
                    System.out.println("   ⚠ Friend not connected locally: " + friendName);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ============================================================
       ⏳ Print TTL Remaining
       ============================================================ */
    private void printTTL(String username, String action) {

        Long ttl = redisTemplate.getExpire("online:user:" + username);

        System.out.println("⏳ TTL " + action + " → "
                + username + " : " + ttl + " seconds remaining");
    }

    /* ============================================================
       🔄 Sync Friend Status On Login
       ============================================================ */
    private void syncFriendStatus(WebSocketSession session, String username) {

        try {
            System.out.println("🔄 Syncing friend status for: " + username);

            String response = webClient
                    .get()
                    .uri("/user/friends/listName/" + username)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) return;

            JsonNode root = mapper.readTree(response);

            for (JsonNode friend : root) {

                String friendName = friend.get("name").asText();

                boolean isOnline =
                        presenceService.isUserOnline(friendName);

                System.out.println("   ➜ Friend: "
                        + friendName + " | Online: " + isOnline);

                if (isOnline) {

                    session.sendMessage(new TextMessage(
                            "{\"type\":\"PRESENCE\",\"username\":\""
                                    + friendName + "\",\"online\":true}"
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}