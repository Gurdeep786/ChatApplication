package com.practice.auth_service.auth_service.service;

import com.practice.auth_service.auth_service.Entity.User;
import com.practice.auth_service.auth_service.dto.RegisterRequest;
import com.practice.auth_service.auth_service.repository.UserRepository;
import com.practice.auth_service.auth_service.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // Inject WebClient.Builder
    private final WebClient webClient; // Make it final and initialize in constructor

    @Autowired
    private JwtUtil jwtUtil;

    // Constructor injection for WebClient.Builder



    // Spring will now inject the @LoadBalanced builder we defined above
    public AuthService(UserRepository userRepository,
                       WebClient.Builder webClientBuilder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        // The http:// prefix is required for the LoadBalancer to trigger
        this.webClient = webClientBuilder.baseUrl("http://USER-SERVICE").build();
    }

    public String login(String username, String password) {

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password");
        }
        System.out.println("user"+user);
        return jwtUtil.generateToken(user.getId(),username);
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());

        // 1. Save locally first to generate the 'id'
        User savedUser = userRepository.save(user);
        // 🔥 Call user-service to create profile
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/profile/create")
                        .queryParam("userId", savedUser.getId()) // Pass the ID
                        .queryParam("username", savedUser.getUsername())
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
