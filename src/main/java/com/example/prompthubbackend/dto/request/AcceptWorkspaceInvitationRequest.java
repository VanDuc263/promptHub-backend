package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptWorkspaceInvitationRequest(@NotBlank String token) {}
