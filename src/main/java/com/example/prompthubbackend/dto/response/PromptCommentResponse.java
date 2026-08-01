package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PromptCommentResponse(
        UUID id,
        UUID parentId,
        UUID userId,
        String author,
        String avatarUrl,
        String content,
        boolean edited,
        boolean mine,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
