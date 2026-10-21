package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PromptActivityResponse(UUID id, String actor, String action,
                                     OffsetDateTime createdAt, String details) {}
