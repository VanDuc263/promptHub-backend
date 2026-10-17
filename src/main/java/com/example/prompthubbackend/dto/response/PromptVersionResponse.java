package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PromptVersionResponse(
        UUID id,
        String version,
        String note,
        String author,
        OffsetDateTime createdAt,
        boolean current,
        String content
) {}
