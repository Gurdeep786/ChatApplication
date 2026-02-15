package com.practice.user_service.controller;

import com.practice.user_service.entity.UserProfile;
import com.practice.user_service.service.FriendService;
import com.practice.user_service.service.UserProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/friends")
public class FriendController {

    private final FriendService service;
    private final UserProfileService userProfileService;

    public FriendController(FriendService service, UserProfileService userProfileService) {
        this.service = service;
        this.userProfileService = userProfileService;
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

    @GetMapping("/listName/{username}")
    public List<UserProfile> listName(@PathVariable String  username) {

        UserProfile user= userProfileService.getFriends(username);
        System.out.println("USER NAME: "+user);
        System.out.println("USER NAME: "+service.getFriends(user.getUserId()));
        return service.getFriends(user.getUserId());

    }
}

