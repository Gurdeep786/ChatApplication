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

    /*
     * ============================================================
     * 1️⃣ RedisTemplate
     * Used for:
     * - Publishing messages
     * - Storing simple string data (if needed)
     * ============================================================
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for readable keys & values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }

    /*
     * ============================================================
     * 2️⃣ Chat Message Channel
     * Used only for chat messages
     * ============================================================
     */
    @Bean
    public ChannelTopic chatTopic() {
        return new ChannelTopic("chat-channel");
    }

    /*
     * ============================================================
     * 3️⃣ Presence Channel
     * Used for:
     * - User ONLINE events
     * - User OFFLINE events
     * ============================================================
     */
    @Bean
    public ChannelTopic presenceTopic() {
        return new ChannelTopic("presence-channel");
    }

    /*
     * ============================================================
     * 4️⃣ Adapter for chat messages
     * Links Redis chat-channel → ChatHandler.handleRedisMessage()
     * ============================================================
     */
    @Bean
    public MessageListenerAdapter chatListenerAdapter(ChatHandler chatHandler) {
        return new MessageListenerAdapter(chatHandler, "handleRedisMessage");
    }

    /*
     * ============================================================
     * 5️⃣ Adapter for presence events
     * Links Redis presence-channel → ChatHandler.handlePresenceEvent()
     * ============================================================
     */
    @Bean
    public MessageListenerAdapter presenceListenerAdapter(ChatHandler chatHandler) {
        return new MessageListenerAdapter(chatHandler, "handlePresenceEvent");
    }

    /*
     * ============================================================
     * 6️⃣ Single Redis Listener Container
     * Listens to:
     * - chat-channel
     * - presence-channel
     *
     * This works in multi-instance environment.
     * ============================================================
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter chatListenerAdapter,
            MessageListenerAdapter presenceListenerAdapter,
            ChannelTopic chatTopic,
            ChannelTopic presenceTopic,
            ChatHandler chatHandler   // 🔥 inject handler
    ) {

        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);

        // 1️⃣ Listen for chat messages
        container.addMessageListener(chatListenerAdapter, chatTopic);

        // 2️⃣ Listen for presence events
        container.addMessageListener(presenceListenerAdapter, presenceTopic);

        // 3️⃣ Listen for Redis TTL expiration events
        container.addMessageListener((message, pattern) -> {

            String expiredKey = message.toString();

            System.out.println("🔥 REDIS EXPIRED EVENT RECEIVED: " + expiredKey);

            if (expiredKey.startsWith("online:user:")) {

                String username =
                        expiredKey.replace("online:user:", "");

                System.out.println("⏳ TTL expired for user: " + username);

                // 🔥 publish offline event
                chatHandler.publishPresence(username, false);
            }

        }, new org.springframework.data.redis.listener.PatternTopic("__keyevent@0__:expired"));

        return container;
    }
}