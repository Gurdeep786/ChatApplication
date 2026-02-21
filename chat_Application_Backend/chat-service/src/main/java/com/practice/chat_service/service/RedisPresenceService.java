package com.practice.chat_service.service;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisPresenceService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "online:user:";
    private static final long TTL_SECONDS = 30;

    public RedisPresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setUserOnline(String username) {
        redisTemplate.opsForValue()
                .set(PREFIX + username, "1",
                        Duration.ofSeconds(TTL_SECONDS));
    }

    public void refreshUserOnline(String username) {
        setUserOnline(username); // reset TTL
    }

    public void setUserOffline(String username) {
        redisTemplate.delete(PREFIX + username);
    }

    public boolean isUserOnline(String username) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(PREFIX + username)
        );
    }
}