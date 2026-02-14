//package com.practice.user_service.controller;
//
//
//import com.practice.user_service.entity.UserProfile;
//
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/user")
//public class UserProfileController {
//
//    private final UserProfileService service;
//
//    public UserProfileController(UserProfileService service) {
//        this.service = service;
//    }
//
//    @GetMapping("/profile/{username}")
//    public UserProfile getProfile(@PathVariable String username) {
//        return service.getOrCreate(username);
//    }
//
//    @PostMapping("/about/{username}")
//    public UserProfile updateAbout(
//            @PathVariable String username,
//            @RequestBody String about) {
//
//        UserProfile profile = service.getOrCreate(username);
//        profile.setAbout(about);
//        return profile;
//    }
//}
