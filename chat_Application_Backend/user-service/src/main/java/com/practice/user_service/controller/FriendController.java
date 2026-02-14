package com.practice.user_service.controller;

import com.practice.user_service.entity.UserProfile;
import com.practice.user_service.service.FriendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/friends")
public class FriendController {

    private final FriendService service;

    public FriendController(FriendService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public void add(@RequestParam Long user,
                    @RequestParam Long friend) {
        service.addFriend(user, friend);
    }

    @DeleteMapping("/remove")
    public void remove(@RequestParam String user,
                       @RequestParam String friend) {
//        service.removeFriend(user, friend);
    }

    @GetMapping("/list/{userId}")
    public List<UserProfile> list(@PathVariable Long userId) {
        return service.getFriends(userId);
    }
}

