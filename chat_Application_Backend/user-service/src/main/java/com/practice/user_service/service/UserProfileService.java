package com.practice.user_service.service;

import com.practice.user_service.entity.Friend;
import com.practice.user_service.entity.UserProfile;
import com.practice.user_service.repository.UserProfileReepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {
    private final UserProfileReepository userRepo;
    private Object username;

    public UserProfileService(UserProfileReepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserProfile getFriends(String username) {

        UserProfile user = (UserProfile) userRepo.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));



        return user;
    }

}
