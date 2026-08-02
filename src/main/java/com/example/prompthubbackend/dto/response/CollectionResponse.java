package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CollectionResponse(
        UUID id,
        String name,
        String description,
        String coverImageUrl,
        String visibility,
        String owner,
        String ownerInitials,
        List<UUID> promptIds,
        long followers,
        long views,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
