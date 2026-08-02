package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String displayName,
        String avatarUrl,
        String bio,
        String websiteUrl,
        String location,
        boolean verified,
        OffsetDateTime joinedAt,
        OffsetDateTime lastActiveAt,
        long publicPromptCount,
        long publicCollectionCount,
        long totalCopies,
        double averageRating,
        long reviewCount
) {
}
