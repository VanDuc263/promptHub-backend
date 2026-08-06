package com.example.prompthubbackend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceInvitationResponse(
        UUID id,
        UUID workspaceId,
        String workspaceName,
        String email,
        String role,
        String status,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt
) {}
