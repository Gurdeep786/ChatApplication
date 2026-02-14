package com.practice.chat_service.config;



import com.practice.chat_service.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        URI uri = request.getURI();
        String query = uri.getQuery(); // token=xxxx

        if (query == null || !query.contains("token=")) {
            return false;
        }

        String token = query.split("token=")[1];

        // validate token here
        if (!jwtUtil.isTokenValid(token)) {
            return false;
        }
        String username = jwtUtil.extractUsername(token);
        System.out.println("username: " + username);
        attributes.put("username", username);
        return true;
    }


    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // not needed
    }
}
