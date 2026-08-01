package com.example.prompthubbackend.dto.response;

import java.util.UUID;

public record WorkspaceSummaryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String avatarUrl,
        boolean personal,
        String role,
        long promptCount,
        long collectionCount,
        long memberCount
) {
}
