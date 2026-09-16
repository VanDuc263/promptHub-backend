package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.CreatePromptRequest;
import com.example.prompthubbackend.dto.response.CreatePromptResponse;
import com.example.prompthubbackend.dto.response.PromptEditorResponse;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptTag;
import com.example.prompthubbackend.model.PromptVersion;
import com.example.prompthubbackend.model.Tag;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.WorkspaceMember;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.model.enums.WorkspaceRole;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.PromptTagRepository;
import com.example.prompthubbackend.repository.PromptVersionRepository;
import com.example.prompthubbackend.repository.TagRepository;
import com.example.prompthubbackend.repository.UserRepository;
import com.example.prompthubbackend.repository.WorkspaceMemberRepository;
import com.example.prompthubbackend.repository.WorkspaceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class PromptCreateService {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PromptRepository promptRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final TagRepository tagRepository;
    private final PromptTagRepository promptTagRepository;
    private final ObjectMapper objectMapper;

    public PromptCreateService(UserRepository userRepository, WorkspaceRepository workspaceRepository,
                               WorkspaceMemberRepository workspaceMemberRepository, PromptRepository promptRepository,
                               PromptVersionRepository promptVersionRepository, TagRepository tagRepository,
                               PromptTagRepository promptTagRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.promptRepository = promptRepository;
        this.promptVersionRepository = promptVersionRepository;
        this.tagRepository = tagRepository;
        this.promptTagRepository = promptTagRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CreatePromptResponse create(String username, CreatePromptRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Workspace workspace = personalWorkspace(user);
        OffsetDateTime now = OffsetDateTime.now();
        PromptStatus status = request.publish() ? PromptStatus.PUBLISHED : PromptStatus.DRAFT;

        Prompt prompt = Prompt.builder()
                .workspace(workspace).author(user).title(request.title().trim())
                .slug(uniqueSlug(workspace, request.title()))
                .shortDescription(blankToNull(request.description())).description(blankToNull(request.description()))
                .visibility(parseVisibility(request.visibility())).status(status)
                .recommendedModel(request.model().trim()).language(request.language().trim())
                .promptType(parsePromptType(request.type()))
                .inputPreviewUrl(blankToNull(request.inputPreviewUrl()))
                .outputPreviewUrl(blankToNull(request.outputPreviewUrl()))
                .viewCount(0L).runCount(0L).favoriteCount(0L).forkCount(0L).isFeatured(false)
                .publishedAt(request.publish() ? now : null).createdAt(now).updatedAt(now)
                .build();
        prompt = promptRepository.save(prompt);

        PromptVersion version = PromptVersion.builder()
                .prompt(prompt).versionNumber(1).content(request.content())
                .systemMessage(blankToNull(request.systemMessage()))
                .variables(toJson(request.variables() == null ? List.of() : request.variables()))
                .modelConfig(toJson(Map.of("model", request.model())))
                .changelog(blankToNull(request.notes())).createdBy(user).createdAt(now).build();
        version = promptVersionRepository.save(version);
        prompt.setCurrentVersionId(version.getId());
        promptRepository.save(prompt);

        for (String rawTag : request.tags() == null ? List.<String>of() : request.tags()) {
            String name = rawTag.trim();
            if (name.isBlank()) continue;
            String slug = slugify(name);
            if (slug.isBlank()) continue;
            Tag tag = tagRepository.findBySlug(slug).orElseGet(() -> tagRepository.save(Tag.builder()
                    .name(name).slug(slug).usageCount(0L).createdAt(now).build()));
            if (promptTagRepository.findByPromptAndTag(prompt, tag).isEmpty()) {
                promptTagRepository.save(PromptTag.builder().prompt(prompt).tag(tag).createdAt(now).build());
            }
        }
        return new CreatePromptResponse(prompt.getId(), prompt.getTitle(), status.name(), "v1");
    }

    @Transactional(readOnly = true)
    public PromptEditorResponse getEditor(String username, UUID promptId) {
        User user = findUser(username);
        Prompt prompt = findOwnedPrompt(promptId, user);
        PromptVersion version = currentVersion(prompt);
        List<String> tags = promptTagRepository.findByPrompt(prompt).stream()
                .map(item -> item.getTag().getName()).distinct().toList();
        return new PromptEditorResponse(
                prompt.getId(), prompt.getTitle(), blankToEmpty(prompt.getShortDescription()), categoryFor(tags),
                visibilityLabel(prompt.getVisibility()), prompt.getRecommendedModel(), prompt.getLanguage(), tags,
                version.getContent(), blankToEmpty(version.getSystemMessage()), readVariables(version.getVariables()),
                blankToEmpty(version.getChangelog()), prompt.getStatus().name(), prompt.getPromptType(),
                blankToEmpty(prompt.getInputPreviewUrl()), blankToEmpty(prompt.getOutputPreviewUrl())
        );
    }

    @Transactional
    public CreatePromptResponse update(String username, UUID promptId, CreatePromptRequest request) {
        User user = findUser(username);
        Prompt prompt = findOwnedPrompt(promptId, user);
        PromptVersion version = currentVersion(prompt);
        OffsetDateTime now = OffsetDateTime.now();
        PromptStatus status = request.publish() ? PromptStatus.PUBLISHED : PromptStatus.DRAFT;

        prompt.setTitle(request.title().trim());
        prompt.setShortDescription(blankToNull(request.description()));
        prompt.setDescription(blankToNull(request.description()));
        prompt.setVisibility(parseVisibility(request.visibility()));
        prompt.setStatus(status);
        prompt.setRecommendedModel(request.model().trim());
        prompt.setLanguage(request.language().trim());
        prompt.setPromptType(parsePromptType(request.type()));
        prompt.setInputPreviewUrl(blankToNull(request.inputPreviewUrl()));
        prompt.setOutputPreviewUrl(blankToNull(request.outputPreviewUrl()));
        prompt.setPublishedAt(status == PromptStatus.PUBLISHED ? Objects.requireNonNullElse(prompt.getPublishedAt(), now) : null);
        prompt.setUpdatedAt(now);

        version.setContent(request.content());
        version.setSystemMessage(blankToNull(request.systemMessage()));
        version.setVariables(toJson(request.variables() == null ? List.of() : request.variables()));
        version.setModelConfig(toJson(Map.of("model", request.model())));
        version.setChangelog(blankToNull(request.notes()));
        promptVersionRepository.save(version);
        replaceTags(prompt, request.tags(), now);
        promptRepository.save(prompt);
        return new CreatePromptResponse(prompt.getId(), prompt.getTitle(), status.name(), "v" + version.getVersionNumber());
    }

    @Transactional
    public void updateVisibility(String username, UUID promptId, String visibility) {
        User user = findUser(username);
        Prompt prompt = findOwnedPrompt(promptId, user);
        PromptVisibility parsed = parseVisibility(visibility);
        if (parsed != PromptVisibility.PRIVATE && parsed != PromptVisibility.PUBLIC) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visibility must be Public or Private");
        }
        prompt.setVisibility(parsed);
        prompt.setUpdatedAt(OffsetDateTime.now());
        promptRepository.save(prompt);
    }

    private User findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Prompt findOwnedPrompt(UUID promptId, User user) {
        Prompt prompt = promptRepository.findById(promptId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"));
        if (!Objects.equals(prompt.getAuthor().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit this prompt");
        }
        return prompt;
    }

    private PromptVersion currentVersion(Prompt prompt) {
        if (prompt.getCurrentVersionId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prompt has no editable version");
        }
        return promptVersionRepository.findById(prompt.getCurrentVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Prompt version not found"));
    }

    private void replaceTags(Prompt prompt, List<String> rawTags, OffsetDateTime now) {
        Map<String, String> requested = new java.util.LinkedHashMap<>();
        for (String rawTag : rawTags == null ? List.<String>of() : rawTags) {
            String name = rawTag.trim();
            if (name.isBlank()) continue;
            String slug = slugify(name);
            if (slug.isBlank()) continue;
            requested.putIfAbsent(slug, name);
        }
        List<PromptTag> existing = promptTagRepository.findByPrompt(prompt);
        existing.stream()
                .filter(item -> !requested.containsKey(item.getTag().getSlug()))
                .forEach(promptTagRepository::delete);
        java.util.Set<String> existingSlugs = existing.stream()
                .map(item -> item.getTag().getSlug())
                .collect(java.util.stream.Collectors.toSet());
        for (Map.Entry<String, String> entry : requested.entrySet()) {
            if (existingSlugs.contains(entry.getKey())) continue;
            String slug = entry.getKey();
            String name = entry.getValue();
            Tag tag = tagRepository.findBySlug(slug).orElseGet(() -> tagRepository.save(Tag.builder()
                    .name(name).slug(slug).usageCount(0L).createdAt(now).build()));
            promptTagRepository.save(PromptTag.builder().prompt(prompt).tag(tag).createdAt(now).build());
        }
    }

    private List<CreatePromptRequest.PromptVariableRequest> readVariables(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (JsonProcessingException exception) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid stored prompt variables"); }
    }

    private String visibilityLabel(PromptVisibility visibility) {
        return visibility == PromptVisibility.PRIVATE ? "Private" : "Public";
    }

    private String categoryFor(List<String> tags) {
        String joined = String.join(" ", tags).toLowerCase(Locale.ROOT);
        if (joined.contains("development") || joined.contains("java") || joined.contains("code")) return "Programming";
        if (joined.contains("marketing")) return "Marketing";
        if (joined.contains("writing") || joined.contains("english")) return "English";
        if (joined.contains("image") || joined.contains("visual") || joined.contains("photo")) return "Image Generation";
        return "Other";
    }

    private Workspace personalWorkspace(User user) {
        return workspaceRepository.findFirstByOwnerAndIsPersonalTrue(user).orElseGet(() -> {
            OffsetDateTime now = OffsetDateTime.now();
            Workspace workspace = workspaceRepository.save(Workspace.builder()
                    .name(user.getDisplayName() + "'s workspace").slug("personal-" + user.getId())
                    .owner(user).isPersonal(true).createdAt(now).updatedAt(now).build());
            workspaceMemberRepository.save(WorkspaceMember.builder().workspace(workspace).user(user)
                    .role(WorkspaceRole.OWNER).joinedAt(now).updatedAt(now).build());
            return workspace;
        });
    }

    private String uniqueSlug(Workspace workspace, String title) {
        String base = slugify(title);
        if (base.isBlank()) base = "untitled-prompt";
        String candidate = base;
        int suffix = 2;
        while (promptRepository.findByWorkspaceAndSlug(workspace, candidate).isPresent()) candidate = base + "-" + suffix++;
        return candidate;
    }

    private String slugify(String value) {
        String slug = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug.substring(0, Math.min(slug.length(), 60));
    }

    private PromptVisibility parseVisibility(String value) {
        try { return PromptVisibility.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid visibility"); }
    }

    private String parsePromptType(String value) {
        String type = value == null || value.isBlank() ? "text" : value.trim().toLowerCase(Locale.ROOT);
        if (!type.equals("text") && !type.equals("image")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prompt type must be text or image");
        }
        return type;
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid prompt configuration"); }
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String blankToEmpty(String value) { return value == null ? "" : value; }
}
