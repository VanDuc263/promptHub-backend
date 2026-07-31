package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.response.SavedPromptResponse;
import com.example.prompthubbackend.service.SavedPromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/saved-prompts")
public class SavedPromptController {
    private final SavedPromptService savedPromptService;

    public SavedPromptController(SavedPromptService savedPromptService) {
        this.savedPromptService = savedPromptService;
    }

    @GetMapping
    public ResponseEntity<List<SavedPromptResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(savedPromptService.getSavedPrompts(authentication.getName()));
    }

    @PostMapping("/{promptId}")
    public ResponseEntity<Void> save(@PathVariable UUID promptId, Authentication authentication) {
        savedPromptService.save(authentication.getName(), promptId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{promptId}")
    public ResponseEntity<Void> remove(@PathVariable UUID promptId, Authentication authentication) {
        savedPromptService.remove(authentication.getName(), promptId);
        return ResponseEntity.noContent().build();
    }
}
