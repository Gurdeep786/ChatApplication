package com.practice.chat_service.repository;

import com.practice.chat_service.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessageEntity, Long> {

    @Query("""
    SELECT m
    FROM ChatMessageEntity m
    WHERE (m.sender = :userA AND m.receiver = :userB)
       OR (m.sender = :userB AND m.receiver = :userA)
    ORDER BY m.timestamp ASC
""")
    List<ChatMessageEntity> findChatHistory(
            @Param("userA") String userA,
            @Param("userB") String userB
    );

}

