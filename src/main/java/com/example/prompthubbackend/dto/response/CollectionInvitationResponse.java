package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionInvitationResponse(UUID collectionId, String collectionName, String email, OffsetDateTime expiresAt) {}
