package com.practice.user_service.controller;


import com.practice.user_service.entity.Friend;
import com.practice.user_service.entity.UserProfile;

import com.practice.user_service.repository.FriendRepository;
import com.practice.user_service.repository.UserProfileReepository;
import com.practice.user_service.service.UserProfileService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserProfileReepository repository;
    private final FriendRepository friendRepository;

    public UserProfileController(UserProfileReepository repository,
                                 FriendRepository friendRepository) {
        this.repository = repository;
        this.friendRepository = friendRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createProfile(@RequestParam Long userId,
                                              @RequestParam String username) {

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setName(username);
        profile.setOnline(true);

        repository.save(profile);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<String> addFriend(@RequestParam String username,
                                            @RequestParam String friendUsername) {

        UserProfile userProfile = (UserProfile) repository
                .findByName(username)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        UserProfile friendProfile = (UserProfile) repository
                .findByName(friendUsername)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        if (userProfile.getId().equals(friendProfile.getId())) {
            throw new RuntimeException("You cannot add yourself as friend");
        }

        Friend friend = new Friend();
        friend.setUserProfile(userProfile);
        friend.setFriendProfile(friendProfile);
        friend.setCreatedAt(LocalDateTime.now());
        Friend friend1 = new Friend();
        friend1.setUserProfile(friendProfile);
        friend1.setFriendProfile(userProfile);
        friend1.setCreatedAt(LocalDateTime.now());

        friendRepository.save(friend);
        friendRepository.save(friend1);

        return ResponseEntity.ok("Friend added successfully");
    }
}