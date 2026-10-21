package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionActivityResponse(UUID id, String actor, String action, String subject, OffsetDateTime createdAt) {}
