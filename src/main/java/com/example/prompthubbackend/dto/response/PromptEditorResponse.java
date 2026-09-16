package com.example.prompthubbackend.dto.response;

import com.example.prompthubbackend.dto.request.CreatePromptRequest;

import java.util.List;
import java.util.UUID;

public record PromptEditorResponse(
        UUID id,
        String title,
        String description,
        String category,
        String visibility,
        String model,
        String language,
        List<String> tags,
        String content,
        String systemMessage,
        List<CreatePromptRequest.PromptVariableRequest> variables,
        String notes,
        String status,
        String type,
        String inputPreviewUrl,
        String outputPreviewUrl
) {
}
