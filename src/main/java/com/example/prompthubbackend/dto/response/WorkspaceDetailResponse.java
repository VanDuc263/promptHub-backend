package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record WorkspaceDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String avatarUrl,
        boolean personal,
        String role,
        String ownerName,
        OffsetDateTime createdAt,
        Overview overview,
        List<Activity> recentActivity
) {
    public record Overview(long members, long prompts, long collections) {
    }

    public record Activity(
            UUID id,
            String actor,
            String action,
            String entityType,
            String entityName,
            OffsetDateTime createdAt,
            boolean mine,
            String oldValues,
            String newValues
    ) {
    }
}
