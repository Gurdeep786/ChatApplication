package com.practice.auth_service.auth_service.service;

import com.practice.auth_service.auth_service.Entity.User;
import com.practice.auth_service.auth_service.repository.UserRepository;
import com.practice.auth_service.auth_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

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
}
