package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.request.CreateCollectionRequest;
import com.example.prompthubbackend.dto.response.CollectionResponse;
import com.example.prompthubbackend.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<CollectionResponse> create(
            @Valid @RequestBody CreateCollectionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(201).body(collectionService.create(authentication.getName(), request));
    }
}
