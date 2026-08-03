package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record AddCollectionPromptsRequest(
        @NotEmpty(message = "Select at least one prompt")
        @Size(max = 100, message = "Cannot add more than 100 prompts at once")
        List<@NotNull UUID> promptIds
) {
}
