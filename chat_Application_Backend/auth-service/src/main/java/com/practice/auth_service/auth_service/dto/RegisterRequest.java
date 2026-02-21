package com.practice.auth_service.auth_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;
    private String password;

    // getters & setters
}