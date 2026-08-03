package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePromptVisibilityRequest(@NotBlank String visibility) {
}
