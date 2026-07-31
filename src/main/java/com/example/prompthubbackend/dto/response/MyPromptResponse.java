package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MyPromptResponse(
        UUID id,
        String title,
        String description,
        List<String> tags,
        String visibility,
        String status,
        String version,
        OffsetDateTime updatedAt,
        long uses,
        long saves,
        String category,
        String author
) {}
