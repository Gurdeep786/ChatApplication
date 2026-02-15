package com.practice.chat_service.service;


import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;



@Service
public class RedisPresenceService {

    private final StringRedisTemplate redisTemplate;

    private static final String ONLINE_USERS_KEY = "online:users";

    public RedisPresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setUserOnline(String username) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, username);
    }

    public void setUserOffline(String username) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, username);
    }

    public boolean isUserOnline(String username) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, username)
        );
    }
}
