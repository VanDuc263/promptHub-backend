package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.response.UserProfileResponse;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptReview;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.CollectionVisibility;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.model.enums.UserStatus;
import com.example.prompthubbackend.repository.CollectionRepository;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.PromptReviewRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserProfileService {
    private final UserRepository userRepository;
    private final PromptRepository promptRepository;
    private final CollectionRepository collectionRepository;
    private final PromptReviewRepository promptReviewRepository;

    public UserProfileService(
            UserRepository userRepository,
            PromptRepository promptRepository,
            CollectionRepository collectionRepository,
            PromptReviewRepository promptReviewRepository
    ) {
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
        this.collectionRepository = collectionRepository;
        this.promptReviewRepository = promptReviewRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrent(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getPublic(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .filter(item -> item.getDeletedAt() == null && item.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        return toResponse(user);
    }

    private UserProfileResponse toResponse(User user) {
        List<Prompt> prompts = promptRepository.findByAuthorAndVisibilityAndStatusAndDeletedAtIsNull(
                user, PromptVisibility.PUBLIC, PromptStatus.PUBLISHED
        );
        List<PromptReview> reviews = prompts.stream()
                .flatMap(prompt -> promptReviewRepository.findByPrompt(prompt).stream())
                .toList();
        long totalCopies = prompts.stream()
                .mapToLong(prompt -> value(prompt.getRunCount()) + value(prompt.getForkCount()))
                .sum();
        double rating = reviews.isEmpty() ? 0 : reviews.stream()
                .mapToInt(review -> review.getRating().intValue())
                .average()
                .orElse(0);
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                firstNonBlank(user.getBio(), "This creator has not added a bio yet."),
                user.getWebsiteUrl(),
                user.getLocation(),
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                prompts.size(),
                collectionRepository.countByOwnerAndVisibilityAndDeletedAtIsNull(user, CollectionVisibility.PUBLIC),
                totalCopies,
                Math.round(rating * 10.0) / 10.0,
                reviews.size()
        );
    }

    private long value(Long value) {
        return value == null ? 0 : value;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "";
    }
}
