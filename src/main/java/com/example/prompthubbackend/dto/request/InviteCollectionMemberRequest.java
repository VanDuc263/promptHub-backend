package com.example.prompthubbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteCollectionMemberRequest(@NotBlank @Email String email) {}
