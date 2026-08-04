package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceCollectionResponse(
        UUID id,
        String name,
        String description,
        String coverImageUrl,
        String visibility,
        int promptCount,
        String ownerInitials,
        OffsetDateTime updatedAt
) {
}
