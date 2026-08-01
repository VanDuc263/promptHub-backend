package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpsertPromptReviewRequest(
        @Min(1) @Max(5) int rating,
        @Size(max = 2000) String content
) {
}
