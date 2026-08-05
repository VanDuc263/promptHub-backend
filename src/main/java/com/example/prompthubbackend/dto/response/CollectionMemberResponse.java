package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionMemberResponse(
        UUID userId,
        String name,
        String initials,
        String email,
        String role,
        OffsetDateTime joinedAt,
        OffsetDateTime lastActiveAt,
        boolean currentUser
) {}
