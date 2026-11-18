package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.response.ExplorePromptResponse;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptReview;
import com.example.prompthubbackend.model.PromptVersion;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.PromptReviewRepository;
import com.example.prompthubbackend.repository.PromptTagRepository;
import com.example.prompthubbackend.repository.PromptVersionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PromptExploreService {
    private final PromptRepository promptRepository;
    private final PromptTagRepository promptTagRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final PromptReviewRepository promptReviewRepository;
    private final ObjectMapper objectMapper;

    public PromptExploreService(
            PromptRepository promptRepository,
            PromptTagRepository promptTagRepository,
            PromptVersionRepository promptVersionRepository,
            PromptReviewRepository promptReviewRepository,
            ObjectMapper objectMapper
    ) {
        this.promptRepository = promptRepository;
        this.promptTagRepository = promptTagRepository;
        this.promptVersionRepository = promptVersionRepository;
        this.promptReviewRepository = promptReviewRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ExplorePromptResponse> getPublicPrompts() {
        return promptRepository
                .findByVisibilityAndStatus(PromptVisibility.PUBLIC, PromptStatus.PUBLISHED)
                .stream()
                .filter(prompt -> prompt.getDeletedAt() == null)
                .sorted(Comparator.comparing(Prompt::getPublishedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExplorePromptResponse getPublicPrompt(java.util.UUID promptId) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(item -> item.getDeletedAt() == null)
                .filter(item -> item.getVisibility() == PromptVisibility.PUBLIC)
                .filter(item -> item.getStatus() == PromptStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"));
        return toResponse(prompt);
    }

    @Transactional(readOnly = true)
    public List<com.example.prompthubbackend.dto.response.PublicPromptVersionResponse> getPublicVersions(java.util.UUID promptId) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(item -> item.getDeletedAt() == null && item.getVisibility() == PromptVisibility.PUBLIC
                        && item.getStatus() == PromptStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"));
        PromptVersion current = prompt.getCurrentVersionId() == null ? null
                : promptVersionRepository.findById(prompt.getCurrentVersionId())
                    .filter(item -> item.getPrompt().getId().equals(promptId)).orElse(null);
        if (current == null) return List.of();
        return promptVersionRepository.findByPromptOrderByVersionNumberDesc(prompt).stream()
                .filter(item -> item.getVersionNumber() <= current.getVersionNumber())
                .map(item -> new com.example.prompthubbackend.dto.response.PublicPromptVersionResponse(
                        item.getId(), "v" + item.getVersionNumber(), firstNonBlank(item.getChangelog(), "No changelog provided."),
                        item.getCreatedBy() == null ? "Former member" : firstNonBlank(item.getCreatedBy().getDisplayName(), item.getCreatedBy().getUsername(), "Former member"),
                        item.getCreatedAt(), item.getId().equals(current.getId()), normalizeNewlines(item.getContent()),
                        normalizeNewlines(item.getSystemMessage()), configuration(item.getVariables(), true),
                        configuration(item.getModelConfig(), false), examples(item), documentation(item)))
                .toList();
    }

    private JsonNode configuration(String value, boolean array) {
        try {
            JsonNode node = objectMapper.readTree(value == null ? "null" : value);
            if (array ? node.isArray() : node.isObject()) return node;
        } catch (Exception ignored) {}
        return array ? objectMapper.createArrayNode() : objectMapper.createObjectNode();
    }

    private ExplorePromptResponse toResponse(Prompt prompt) {
        List<String> tags = promptTagRepository.findByPrompt(prompt).stream()
                .map(promptTag -> promptTag.getTag().getName())
                .distinct()
                .toList();
        PromptVersion version = prompt.getCurrentVersionId() == null
                ? null
                : promptVersionRepository.findById(prompt.getCurrentVersionId()).orElse(null);
        List<PromptReview> reviews = promptReviewRepository.findByPrompt(prompt);
        double rating = reviews.isEmpty()
                ? 0.0
                : reviews.stream().mapToInt(review -> review.getRating().intValue()).average().orElse(0.0);

        return new ExplorePromptResponse(
                prompt.getId(),
                prompt.getTitle(),
                firstNonBlank(prompt.getShortDescription(), prompt.getDescription(), "No description yet."),
                categoryFor(tags),
                tags,
                List.of(modelName(prompt.getRecommendedModel())),
                prompt.getAuthor().getDisplayName(),
                initials(prompt.getAuthor().getDisplayName()),
                prompt.getPublishedAt(),
                value(prompt.getRunCount()) + value(prompt.getForkCount()),
                value(prompt.getFavoriteCount()),
                value(prompt.getFavoriteCount()),
                Math.round(rating * 10.0) / 10.0,
                tokenLimit(version),
                "Public",
                version == null ? "" : normalizeNewlines(firstNonBlank(version.getSystemMessage(), "")),
                version == null ? "" : normalizeNewlines(version.getContent()),
                variableNames(version),
                Boolean.TRUE.equals(prompt.getIsFeatured()),
                prompt.getPromptType() == null ? "text" : prompt.getPromptType(),
                prompt.getInputPreviewUrl(),
                prompt.getOutputPreviewUrl(),
                examples(version),
                documentation(version)
        );
    }

    private List<ExplorePromptResponse.DocumentationSection> documentation(PromptVersion version) {
        if (version == null || version.getDocumentation() == null) return List.of();
        try {
            JsonNode items = objectMapper.readTree(version.getDocumentation());
            if (!items.isArray()) return List.of();
            List<ExplorePromptResponse.DocumentationSection> sections = new ArrayList<>();
            for (JsonNode item : items) {
                String title = item.path("title").asText("").trim();
                String content = item.path("content").asText("").trim();
                if (!title.isBlank() && !content.isBlank()) {
                    sections.add(new ExplorePromptResponse.DocumentationSection(title, normalizeNewlines(content)));
                }
            }
            return sections;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<ExplorePromptResponse.Example> examples(PromptVersion version) {
        if (version == null || version.getExamples() == null) return List.of();
        try {
            JsonNode items = objectMapper.readTree(version.getExamples());
            if (!items.isArray()) return List.of();
            List<ExplorePromptResponse.Example> result = new ArrayList<>();
            for (JsonNode item : items) {
                String input = item.path("input").asText("");
                String output = item.path("output").asText("");
                if (input.isBlank() || output.isBlank()) continue;
                result.add(new ExplorePromptResponse.Example(
                        item.path("id").asText("example-" + (result.size() + 1)),
                        item.path("title").asText("Example " + (result.size() + 1)),
                        normalizeNewlines(input), normalizeNewlines(output)));
            }
            return result;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<String> variableNames(PromptVersion version) {
        if (version == null || version.getVariables() == null) return List.of();
        try {
            JsonNode variables = objectMapper.readTree(version.getVariables());
            List<String> names = new ArrayList<>();
            if (variables.isArray()) {
                variables.forEach(variable -> {
                    String name = variable.path("name").asText("");
                    if (!name.isBlank()) names.add(name);
                });
            }
            return names;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private int tokenLimit(PromptVersion version) {
        if (version == null || version.getModelConfig() == null) return 0;
        try {
            return objectMapper.readTree(version.getModelConfig()).path("maxTokens").asInt(0);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String categoryFor(List<String> tags) {
        String joined = String.join(" ", tags).toLowerCase(Locale.ROOT);
        if (joined.contains("development") || joined.contains("java") || joined.contains("code")) return "Programming";
        if (joined.contains("marketing")) return "Marketing";
        if (joined.contains("writing")) return "Writing";
        if (joined.contains("design")) return "Productivity";
        if (joined.contains("education")) return "Education";
        if (joined.contains("business")) return "Business";
        if (joined.contains("image") || joined.contains("visual") || joined.contains("photo")) return "Image Generation";
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
        if (name == null || name.isBlank()) return "PH";
        String[] parts = name.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : ""))
                .toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "";
    }

    private String normalizeNewlines(String value) {
        if (value == null) return "";
        return value
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n");
    }

    private long value(Long value) {
        return value == null ? 0 : value;
    }
}
