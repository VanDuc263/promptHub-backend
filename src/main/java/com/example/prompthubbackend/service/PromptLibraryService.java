package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.response.MyPromptResponse;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptVersion;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.PromptTagRepository;
import com.example.prompthubbackend.repository.PromptVersionRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PromptLibraryService {
    private final PromptRepository promptRepository;
    private final PromptTagRepository promptTagRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final UserRepository userRepository;

    public PromptLibraryService(
            PromptRepository promptRepository,
            PromptTagRepository promptTagRepository,
            PromptVersionRepository promptVersionRepository,
            UserRepository userRepository
    ) {
        this.promptRepository = promptRepository;
        this.promptTagRepository = promptTagRepository;
        this.promptVersionRepository = promptVersionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<MyPromptResponse> getMyPrompts(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return promptRepository.findByAuthor(user).stream()
                .filter(prompt -> prompt.getDeletedAt() == null)
                .filter(prompt -> prompt.getStatus() != PromptStatus.ARCHIVED)
                .sorted(Comparator.comparing(Prompt::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    private MyPromptResponse toResponse(Prompt prompt) {
        List<String> tags = promptTagRepository.findByPrompt(prompt).stream()
                .map(promptTag -> promptTag.getTag().getName())
                .distinct()
                .toList();
        PromptVersion version = prompt.getCurrentVersionId() == null
                ? null
                : promptVersionRepository.findById(prompt.getCurrentVersionId()).orElse(null);

        return new MyPromptResponse(
                prompt.getId(),
                prompt.getTitle(),
                firstNonBlank(prompt.getShortDescription(), prompt.getDescription(), "No description yet."),
                tags,
                visibilityLabel(prompt.getVisibility()),
                prompt.getStatus() == PromptStatus.DRAFT ? "Draft" : "Published",
                version == null ? "—" : "v" + version.getVersionNumber(),
                prompt.getUpdatedAt(),
                value(prompt.getRunCount()),
                value(prompt.getFavoriteCount()),
                categoryFor(tags),
                firstNonBlank(prompt.getAuthor().getDisplayName(), prompt.getAuthor().getUsername())
        );
    }

    private String visibilityLabel(PromptVisibility visibility) {
        return switch (visibility) {
            case PRIVATE -> "Private";
            case WORKSPACE -> "Team";
            case PUBLIC, UNLISTED -> "Public";
        };
    }

    private String categoryFor(List<String> tags) {
        String joined = String.join(" ", tags).toLowerCase(Locale.ROOT);
        if (joined.contains("development") || joined.contains("java") || joined.contains("code")) return "Programming";
        if (joined.contains("marketing")) return "Marketing";
        if (joined.contains("writing") || joined.contains("english")) return "English";
        return "Other";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "";
    }

    private long value(Long value) {
        return value == null ? 0 : value;
    }
}
