package com.practice.user_service.service;

import com.practice.user_service.entity.Friend;
import com.practice.user_service.entity.UserProfile;
import com.practice.user_service.repository.FriendRepository;

import com.practice.user_service.repository.UserProfileReepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    private final UserProfileReepository userRepo;
    private final FriendRepository friendRepo;

    public FriendService(UserProfileReepository userRepo,
                         FriendRepository friendRepo) {
        this.userRepo = userRepo;
        this.friendRepo = friendRepo;
    }

    // ➕ ADD FRIEND (ID-based ONLY)
    @Transactional
    public void addFriend(Long userId, Long friendUserId) {

        if (userId.equals(friendUserId)) {
            throw new IllegalArgumentException("Cannot add yourself as friend");
        }

        UserProfile user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile friend = userRepo.findByUserId(friendUserId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        // Prevent duplicate friendship
        if (friendRepo.existsByUserProfileAndFriendProfile(user, friend)) {
            return;
        }

        Friend f1 = new Friend();
        f1.setUserProfile(user);
        f1.setFriendProfile(friend);
        f1.setCreatedAt(LocalDateTime.now());

        Friend f2 = new Friend();
        f2.setUserProfile(friend);
        f2.setFriendProfile(user);
        f2.setCreatedAt(LocalDateTime.now());

        friendRepo.save(f1);
        friendRepo.save(f2);
    }

    // ❌ REMOVE FRIEND (ID-based ONLY)
    @Transactional
    public void removeFriend(Long userId, Long friendUserId) {

        UserProfile user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile friend = userRepo.findByUserId(friendUserId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        friendRepo.deleteByUserProfileAndFriendProfile(user, friend);
        friendRepo.deleteByUserProfileAndFriendProfile(friend, user);
    }

    // 📃 LIST FRIENDS
    public List<UserProfile> getFriends(Long userId) {

        UserProfile user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return friendRepo.findByUserProfile(user)
                .stream()
                .map(Friend::getFriendProfile)
                .toList();
    }
}
