package com.example.prompthubbackend.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PublicPromptVersionResponse(
        UUID id, String version, String note, String author, OffsetDateTime createdAt,
        boolean current, String content, String systemMessage, JsonNode variables,
        JsonNode modelConfig, List<ExplorePromptResponse.Example> examples,
        List<ExplorePromptResponse.DocumentationSection> documentation
) {}
