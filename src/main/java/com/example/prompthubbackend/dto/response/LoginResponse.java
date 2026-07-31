package com.example.prompthubbackend.dto.response;

import com.example.prompthubbackend.model.enums.Role;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public record UserResponse(
            UUID id,
            String username,
            String email,
            String displayName,
            String avatarUrl,
            Role role
    ) {
    }
}
