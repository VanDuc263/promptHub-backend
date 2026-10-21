package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.request.AddCollectionPromptsRequest;
import com.example.prompthubbackend.dto.request.CreateCollectionRequest;
import com.example.prompthubbackend.dto.request.InviteCollectionMemberRequest;
import com.example.prompthubbackend.dto.request.AcceptCollectionInvitationRequest;
import com.example.prompthubbackend.dto.response.CollectionResponse;
import com.example.prompthubbackend.dto.response.CollectionInvitationResponse;
import com.example.prompthubbackend.dto.response.CollectionMemberResponse;
import com.example.prompthubbackend.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> mine(Authentication authentication) {
        return ResponseEntity.ok(collectionService.getMine(authentication.getName()));
    }

    @GetMapping("/public")
    public ResponseEntity<List<CollectionResponse>> publicCollections() {
        return ResponseEntity.ok(collectionService.getPublic());
    }

    @GetMapping("/public/{collectionId}")
    public ResponseEntity<CollectionResponse> publicCollection(@PathVariable UUID collectionId) {
        return ResponseEntity.ok(collectionService.getPublic(collectionId));
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> create(
            @Valid @RequestBody CreateCollectionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(201).body(collectionService.create(authentication.getName(), request));
    }

    @PostMapping("/{collectionId}/invitations")
    public ResponseEntity<CollectionInvitationResponse> invite(
            @PathVariable UUID collectionId,
            @Valid @RequestBody InviteCollectionMemberRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(201).body(collectionService.invite(authentication.getName(), collectionId, request));
    }

    @GetMapping("/{collectionId}/activity")
    public ResponseEntity<List<com.example.prompthubbackend.dto.response.CollectionActivityResponse>> activity(
            @PathVariable UUID collectionId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            Authentication authentication
    ) {
        return ResponseEntity.ok(collectionService.getActivity(authentication.getName(), collectionId, page));
    }

    @GetMapping("/{collectionId}/members")
    public ResponseEntity<List<CollectionMemberResponse>> members(
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(collectionService.getMembers(authentication.getName(), collectionId));
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<CollectionInvitationResponse> accept(
            @Valid @RequestBody AcceptCollectionInvitationRequest request
    ) {
        return ResponseEntity.ok(collectionService.acceptInvitation(request.token()));
    }

    @PostMapping("/{collectionId}/prompts")
    public ResponseEntity<CollectionResponse> addPrompts(
            @PathVariable UUID collectionId,
            @Valid @RequestBody AddCollectionPromptsRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(collectionService.addPrompts(authentication.getName(), collectionId, request.promptIds()));
    }

    @DeleteMapping("/{collectionId}/prompts/{promptId}")
    public ResponseEntity<Void> removePrompt(
            @PathVariable UUID collectionId,
            @PathVariable UUID promptId,
            Authentication authentication
    ) {
        collectionService.removePrompt(authentication.getName(), collectionId, promptId);
        return ResponseEntity.noContent().build();
    }
}
