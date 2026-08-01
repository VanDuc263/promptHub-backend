package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.request.CreatePromptCommentRequest;
import com.example.prompthubbackend.dto.request.UpsertPromptReviewRequest;
import com.example.prompthubbackend.dto.response.PromptCommentResponse;
import com.example.prompthubbackend.dto.response.PromptReviewResponse;
import com.example.prompthubbackend.service.PromptFeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prompts/{promptId}")
public class PromptFeedbackController {
    private final PromptFeedbackService feedbackService;

    public PromptFeedbackController(PromptFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/comments")
    public ResponseEntity<List<PromptCommentResponse>> comments(@PathVariable UUID promptId, Authentication authentication) {
        return ResponseEntity.ok(feedbackService.getComments(authentication == null ? null : authentication.getName(), promptId));
    }

    @PostMapping("/comments")
    public ResponseEntity<PromptCommentResponse> addComment(@PathVariable UUID promptId,
                                                             @Valid @RequestBody CreatePromptCommentRequest request,
                                                             Authentication authentication) {
        return ResponseEntity.status(201).body(feedbackService.addComment(authentication.getName(), promptId, request));
    }

    @GetMapping("/reviews")
    public ResponseEntity<PromptReviewResponse> reviews(@PathVariable UUID promptId, Authentication authentication) {
        return ResponseEntity.ok(feedbackService.getReviews(authentication == null ? null : authentication.getName(), promptId));
    }

    @PutMapping("/reviews/me")
    public ResponseEntity<PromptReviewResponse> review(@PathVariable UUID promptId,
                                                       @Valid @RequestBody UpsertPromptReviewRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(feedbackService.upsertReview(authentication.getName(), promptId, request));
    }
}
