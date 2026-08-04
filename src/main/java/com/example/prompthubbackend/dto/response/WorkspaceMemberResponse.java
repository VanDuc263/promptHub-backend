package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceMemberResponse(
        UUID id,
        UUID userId,
        String name,
        String username,
        String email,
        String avatarUrl,
        String role,
        OffsetDateTime joinedAt,
        OffsetDateTime lastActiveAt,
        boolean currentUser
) {
}
