package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.response.ExplorePromptResponse;
import com.example.prompthubbackend.dto.response.MyPromptResponse;
import com.example.prompthubbackend.dto.request.CreatePromptRequest;
import com.example.prompthubbackend.dto.response.CreatePromptResponse;
import com.example.prompthubbackend.service.PromptExploreService;
import com.example.prompthubbackend.service.PromptLibraryService;
import com.example.prompthubbackend.service.PromptCreateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final PromptCreateService promptCreateService;

    public PromptController(PromptExploreService promptExploreService, PromptLibraryService promptLibraryService, PromptCreateService promptCreateService) {
        this.promptExploreService = promptExploreService;
        this.promptLibraryService = promptLibraryService;
        this.promptCreateService = promptCreateService;
    }

    @GetMapping("/explore")
    public ResponseEntity<List<ExplorePromptResponse>> explore() {
        return ResponseEntity.ok(promptExploreService.getPublicPrompts());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyPromptResponse>> mine(Authentication authentication) {
        return ResponseEntity.ok(promptLibraryService.getMyPrompts(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<CreatePromptResponse> create(@Valid @RequestBody CreatePromptRequest request, Authentication authentication) {
        return ResponseEntity.status(201).body(promptCreateService.create(authentication.getName(), request));
    }

    @GetMapping("/{promptId}")
    public ResponseEntity<ExplorePromptResponse> detail(@PathVariable UUID promptId) {
        return ResponseEntity.ok(promptExploreService.getPublicPrompt(promptId));
    }
}
