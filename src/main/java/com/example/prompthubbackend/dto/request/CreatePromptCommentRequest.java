package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePromptCommentRequest(
        @NotBlank @Size(max = 5000) String content,
        UUID parentId
) {
}
