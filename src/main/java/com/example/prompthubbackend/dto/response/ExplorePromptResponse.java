package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ExplorePromptResponse(
        UUID id,
        String title,
        String description,
        String category,
        List<String> tags,
        List<String> models,
        String author,
        String authorInitials,
        OffsetDateTime publishedAt,
        long copies,
        long likes,
        long saves,
        double rating,
        int tokens,
        String visibility,
        String snippet,
        List<String> variables,
        boolean featured
) {
}
