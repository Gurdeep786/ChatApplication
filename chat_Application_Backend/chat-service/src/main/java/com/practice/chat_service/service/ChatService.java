package com.practice.chat_service.service;


import com.practice.chat_service.model.ChatMessageEntity;
import com.practice.chat_service.repository.ChatMessageRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository repository;

    public ChatService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    public void saveMessage(
            String sender,
            String receiver,
            String content
           ) {

        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSender(sender);
        entity.setReceiver(receiver);
        entity.setContent(content);
        entity.setTimestamp(LocalDateTime.now());

        repository.save(entity);
    }

    public List<ChatMessageEntity> getChatHistory(String userA, String userB) {
        return repository.findChatHistory(userA, userB);
    }
}

