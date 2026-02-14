package com.practice.user_service.repository;

import com.practice.user_service.entity.Friend;
import com.practice.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    boolean existsByUserProfileAndFriendProfile(
            UserProfile userProfile,
            UserProfile friendProfile
    );

    void deleteByUserProfileAndFriendProfile(
            UserProfile userProfile,
            UserProfile friendProfile
    );

    List<Friend> findByUserProfile(UserProfile userProfile);
}



