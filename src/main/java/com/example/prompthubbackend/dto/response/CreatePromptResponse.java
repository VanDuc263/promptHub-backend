package com.example.prompthubbackend.dto.response;

import java.util.UUID;

public record CreatePromptResponse(UUID id, String title, String status, String version) {}
