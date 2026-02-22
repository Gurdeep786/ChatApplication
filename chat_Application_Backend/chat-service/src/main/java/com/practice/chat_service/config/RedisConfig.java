package com.practice.chat_service.config;

import com.practice.chat_service.handler.ChatHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // ✅ RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // ✅ Redis topic for chat messages
    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("chat-channel");
    }

    // ✅ Adapter to connect Redis Pub/Sub to ChatHandler method
    @Bean
    public MessageListenerAdapter listenerAdapter(ChatHandler chatHandler) {
        return new MessageListenerAdapter(chatHandler, "handleRedisMessage");
    }

    // ✅ SINGLE Redis container for both chat + TTL expiration
    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic topic,
            ChatHandler chatHandler) {

        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);

        // 🔹 1. Listen to chat-channel (existing)
        container.addMessageListener(listenerAdapter, topic);

        // 🔹 2. Listen to TTL expiration events
        container.addMessageListener((message, pattern) -> {

            String expiredKey = message.toString();

            if (expiredKey.startsWith("online:user:")) {

                String username =
                        expiredKey.replace("online:user:", "");

                System.out.println("⚫ TTL expired for: " + username);

                chatHandler.broadcastPresence(username, false);
            }

        }, new PatternTopic("__keyevent@0__:expired"));

        return container;
    }
}