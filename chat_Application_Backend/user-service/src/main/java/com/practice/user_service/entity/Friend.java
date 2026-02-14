package com.practice.user_service.entity;
import com.practice.user_service.entity.UserProfile;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;



@Data
@Entity
@Table(name = "friend")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne
    @JoinColumn(name = "friend_profile_id", nullable = false)
    private UserProfile friendProfile;

    private LocalDateTime createdAt;
}


