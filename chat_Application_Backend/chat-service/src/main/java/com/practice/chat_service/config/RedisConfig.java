//package com.practice.chat_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(
//            RedisConnectionFactory connectionFactory) {
//
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//        return template;
//    }
//}
//
package com.practice.chat_service.config;

import com.practice.chat_service.handler.ChatHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Better serializers for JSON data
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // 1. Define the Channel Name
    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("chat-channel");
    }

    // 2. The "Listener" - This tells Redis to call ChatHandler when a message arrives
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic());
        return container;
    }

    // 3. The "Adapter" - Links the Redis message to a specific method in your ChatHandler
    @Bean
    public MessageListenerAdapter listenerAdapter(ChatHandler chatHandler) {
        // We will create a method named "handleRedisMessage" in ChatHandler
        System.out.println("chatHandler ="+chatHandler);
        return new MessageListenerAdapter(chatHandler, "handleRedisMessage");
    }
}