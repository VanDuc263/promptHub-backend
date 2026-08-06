package com.example.prompthubbackend.dto.request;

import com.example.prompthubbackend.model.enums.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteWorkspaceMemberRequest(
        @NotBlank @Email String email,
        @NotNull WorkspaceRole role,
        @Size(max = 500) String message
) {}
