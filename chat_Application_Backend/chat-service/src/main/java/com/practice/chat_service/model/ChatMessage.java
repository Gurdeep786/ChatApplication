package com.practice.chat_service.model;

import lombok.Data;

@Data
public class ChatMessage {

    private String sender;
    private String receiver;
    private String content;
}
