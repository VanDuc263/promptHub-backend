package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptCollectionInvitationRequest(@NotBlank String token) {}
