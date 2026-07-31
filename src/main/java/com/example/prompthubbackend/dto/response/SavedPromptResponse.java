package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SavedPromptResponse(
        UUID id,
        String title,
        String description,
        String author,
        String authorInitials,
        String category,
        List<String> tags,
        List<String> models,
        String version,
        double rating,
        long copies,
        long forks,
        OffsetDateTime updatedAt,
        OffsetDateTime savedAt,
        String visibility,
        String language
) {}
