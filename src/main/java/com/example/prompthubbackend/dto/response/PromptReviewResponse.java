package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PromptReviewResponse(
        double averageRating,
        long reviewCount,
        Review currentUserReview,
        List<Review> reviews
) {
    public record Review(
            UUID id,
            UUID userId,
            String author,
            String avatarUrl,
            short rating,
            String content,
            boolean edited,
            boolean mine,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }
}
