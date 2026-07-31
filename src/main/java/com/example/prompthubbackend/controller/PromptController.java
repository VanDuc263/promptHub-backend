package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.response.ExplorePromptResponse;
import com.example.prompthubbackend.service.PromptExploreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {
    private final PromptExploreService promptExploreService;

    public PromptController(PromptExploreService promptExploreService) {
        this.promptExploreService = promptExploreService;
    }

    @GetMapping("/explore")
    public ResponseEntity<List<ExplorePromptResponse>> explore() {
        return ResponseEntity.ok(promptExploreService.getPublicPrompts());
    }

    @GetMapping("/{promptId}")
    public ResponseEntity<ExplorePromptResponse> detail(@PathVariable UUID promptId) {
        return ResponseEntity.ok(promptExploreService.getPublicPrompt(promptId));
    }
}
