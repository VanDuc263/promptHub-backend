package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.response.ExplorePromptResponse;
import com.example.prompthubbackend.dto.response.MyPromptResponse;
import com.example.prompthubbackend.service.PromptExploreService;
import com.example.prompthubbackend.service.PromptLibraryService;
import org.springframework.security.core.Authentication;
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
    private final PromptLibraryService promptLibraryService;

    public PromptController(PromptExploreService promptExploreService, PromptLibraryService promptLibraryService) {
        this.promptExploreService = promptExploreService;
        this.promptLibraryService = promptLibraryService;
    }

    @GetMapping("/explore")
    public ResponseEntity<List<ExplorePromptResponse>> explore() {
        return ResponseEntity.ok(promptExploreService.getPublicPrompts());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyPromptResponse>> mine(Authentication authentication) {
        return ResponseEntity.ok(promptLibraryService.getMyPrompts(authentication.getName()));
    }

    @GetMapping("/{promptId}")
    public ResponseEntity<ExplorePromptResponse> detail(@PathVariable UUID promptId) {
        return ResponseEntity.ok(promptExploreService.getPublicPrompt(promptId));
    }
}
