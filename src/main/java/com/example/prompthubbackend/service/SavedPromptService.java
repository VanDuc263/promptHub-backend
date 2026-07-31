package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.response.SavedPromptResponse;
import com.example.prompthubbackend.model.Favorite;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptReview;
import com.example.prompthubbackend.model.PromptVersion;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.repository.FavoriteRepository;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.PromptReviewRepository;
import com.example.prompthubbackend.repository.PromptTagRepository;
import com.example.prompthubbackend.repository.PromptVersionRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SavedPromptService {
    private final FavoriteRepository favoriteRepository;
    private final PromptRepository promptRepository;
    private final PromptTagRepository promptTagRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final PromptReviewRepository promptReviewRepository;
    private final UserRepository userRepository;

    public SavedPromptService(FavoriteRepository favoriteRepository, PromptRepository promptRepository,
                              PromptTagRepository promptTagRepository, PromptVersionRepository promptVersionRepository,
                              PromptReviewRepository promptReviewRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.promptRepository = promptRepository;
        this.promptTagRepository = promptTagRepository;
        this.promptVersionRepository = promptVersionRepository;
        this.promptReviewRepository = promptReviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SavedPromptResponse> getSavedPrompts(String username) {
        User user = requireUser(username);
        return favoriteRepository.findByUser(user).stream()
                .filter(favorite -> canAccess(favorite.getPrompt(), user))
                .sorted(Comparator.comparing(Favorite::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void save(String username, UUID promptId) {
        User user = requireUser(username);
        Prompt prompt = promptRepository.findById(promptId)
                .filter(item -> canAccess(item, user))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"));
        if (favoriteRepository.findByUserAndPrompt(user, prompt).isEmpty()) {
            favoriteRepository.save(Favorite.builder().user(user).prompt(prompt).createdAt(OffsetDateTime.now()).build());
        }
    }

    @Transactional
    public void remove(String username, UUID promptId) {
        User user = requireUser(username);
        promptRepository.findById(promptId)
                .flatMap(prompt -> favoriteRepository.findByUserAndPrompt(user, prompt))
                .ifPresent(favoriteRepository::delete);
    }

    private SavedPromptResponse toResponse(Favorite favorite) {
        Prompt prompt = favorite.getPrompt();
        List<String> tags = promptTagRepository.findByPrompt(prompt).stream()
                .map(promptTag -> promptTag.getTag().getName()).distinct().toList();
        PromptVersion version = prompt.getCurrentVersionId() == null ? null
                : promptVersionRepository.findById(prompt.getCurrentVersionId()).orElse(null);
        List<PromptReview> reviews = promptReviewRepository.findByPrompt(prompt);
        double rating = reviews.stream().mapToInt(review -> review.getRating().intValue()).average().orElse(0);
        String author = firstNonBlank(prompt.getAuthor().getDisplayName(), prompt.getAuthor().getUsername());
        return new SavedPromptResponse(prompt.getId(), prompt.getTitle(),
                firstNonBlank(prompt.getShortDescription(), prompt.getDescription(), "No description yet."),
                author, initials(author), categoryFor(tags), tags,
                List.of(modelName(prompt.getRecommendedModel())),
                version == null ? "—" : "v" + version.getVersionNumber(),
                Math.round(rating * 10.0) / 10.0,
                value(prompt.getRunCount()) + value(prompt.getForkCount()), value(prompt.getForkCount()),
                prompt.getUpdatedAt(), favorite.getCreatedAt(),
                prompt.getVisibility() == PromptVisibility.PUBLIC ? "Public" : "Private",
                firstNonBlank(prompt.getLanguage(), "English"));
    }

    private boolean canAccess(Prompt prompt, User user) {
        if (prompt.getDeletedAt() != null || prompt.getStatus() == PromptStatus.ARCHIVED) return false;
        return (prompt.getVisibility() == PromptVisibility.PUBLIC && prompt.getStatus() == PromptStatus.PUBLISHED)
                || prompt.getAuthor().getId().equals(user.getId());
    }

    private User requireUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String categoryFor(List<String> tags) {
        String joined = String.join(" ", tags).toLowerCase(Locale.ROOT);
        if (joined.contains("development") || joined.contains("java") || joined.contains("code")) return "Programming";
        if (joined.contains("marketing")) return "Marketing";
        if (joined.contains("writing")) return "Writing";
        if (joined.contains("education")) return "Education";
        if (joined.contains("business")) return "Business";
        return "Prompt Engineering";
    }

    private String modelName(String model) {
        if (model == null || model.isBlank()) return "Any model";
        String normalized = model.toLowerCase(Locale.ROOT);
        if (normalized.contains("gpt")) return "GPT-5";
        if (normalized.contains("claude")) return "Claude";
        if (normalized.contains("gemini")) return "Gemini";
        if (normalized.contains("deepseek")) return "DeepSeek";
        if (normalized.contains("grok")) return "Grok";
        return model;
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : ""))
                .toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "";
    }

    private long value(Long value) { return value == null ? 0 : value; }
}
