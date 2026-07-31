package com.example.prompthubbackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePromptRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 300) String description,
        @NotBlank String visibility,
        @NotBlank String model,
        @NotBlank @Size(max = 20) String language,
        @Size(max = 10) List<@Size(max = 50) String> tags,
        @NotBlank String content,
        String systemMessage,
        @Valid List<PromptVariableRequest> variables,
        @Size(max = 500) String notes,
        boolean publish
) {
    public record PromptVariableRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 120) String label,
            @NotBlank String type,
            boolean required,
            @Size(max = 300) String placeholder,
            List<String> options
    ) {}
}
