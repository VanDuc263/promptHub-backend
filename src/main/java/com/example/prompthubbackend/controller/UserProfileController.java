package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.response.UserProfileResponse;
import com.example.prompthubbackend.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getCurrent(authentication.getName()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> publicProfile(@PathVariable String username) {
        return ResponseEntity.ok(userProfileService.getPublic(username));
    }
}
