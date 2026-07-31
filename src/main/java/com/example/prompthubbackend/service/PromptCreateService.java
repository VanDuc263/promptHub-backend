package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.CreatePromptRequest;
import com.example.prompthubbackend.dto.response.CreatePromptResponse;
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

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid prompt configuration"); }
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
