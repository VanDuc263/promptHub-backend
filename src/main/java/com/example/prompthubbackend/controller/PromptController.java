package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.response.ExplorePromptResponse;
import com.example.prompthubbackend.dto.response.MyPromptResponse;
import com.example.prompthubbackend.dto.request.CreatePromptRequest;
import com.example.prompthubbackend.dto.request.CreatePromptVersionRequest;
import com.example.prompthubbackend.dto.request.UpdatePromptVisibilityRequest;
import com.example.prompthubbackend.dto.response.CreatePromptResponse;
import com.example.prompthubbackend.dto.response.PromptEditorResponse;
import com.example.prompthubbackend.dto.response.PromptVersionResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;

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

    @GetMapping("/{promptId}/versions")
    public ResponseEntity<List<PromptVersionResponse>> versions(@PathVariable UUID promptId, Authentication authentication) {
        return ResponseEntity.ok(promptLibraryService.getPromptVersions(authentication.getName(), promptId));
    }

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<CreatePromptResponse> createVersion(
            @PathVariable UUID promptId,
            @Valid @RequestBody CreatePromptVersionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(201).body(promptCreateService.createVersion(authentication.getName(), promptId, request));
    }

    @PostMapping
    public ResponseEntity<CreatePromptResponse> create(@Valid @RequestBody CreatePromptRequest request, Authentication authentication) {
        return ResponseEntity.status(201).body(promptCreateService.create(authentication.getName(), request));
    }

    @GetMapping("/{promptId}/editor")
    public ResponseEntity<PromptEditorResponse> editor(@PathVariable UUID promptId, Authentication authentication) {
        return ResponseEntity.ok(promptCreateService.getEditor(authentication.getName(), promptId));
    }

    @PutMapping("/{promptId}")
    public ResponseEntity<CreatePromptResponse> update(
            @PathVariable UUID promptId,
            @Valid @RequestBody CreatePromptRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(promptCreateService.update(authentication.getName(), promptId, request));
    }

    @PatchMapping("/{promptId}/visibility")
    public ResponseEntity<Void> updateVisibility(
            @PathVariable UUID promptId,
            @Valid @RequestBody UpdatePromptVisibilityRequest request,
            Authentication authentication
    ) {
        promptCreateService.updateVisibility(authentication.getName(), promptId, request.visibility());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{promptId}")
    public ResponseEntity<ExplorePromptResponse> detail(@PathVariable UUID promptId) {
        return ResponseEntity.ok(promptExploreService.getPublicPrompt(promptId));
    }
}
