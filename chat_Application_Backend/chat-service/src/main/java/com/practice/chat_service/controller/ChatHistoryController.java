package com.practice.chat_service.controller;


import com.practice.chat_service.model.ChatMessageEntity;
import com.practice.chat_service.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatHistoryController {

    private final ChatService chatService;

    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/history")
    public List<ChatMessageEntity> getChatHistory(
            @RequestParam String userA,
            @RequestParam String userB) {

        return chatService.getChatHistory(userA, userB);
    }
}
