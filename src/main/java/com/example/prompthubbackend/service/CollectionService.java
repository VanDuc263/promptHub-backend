package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.CreateCollectionRequest;
import com.example.prompthubbackend.dto.response.CollectionResponse;
import com.example.prompthubbackend.model.Collection;
import com.example.prompthubbackend.model.CollectionPrompt;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.CollectionVisibility;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.repository.CollectionPromptRepository;
import com.example.prompthubbackend.repository.CollectionRepository;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionPromptRepository collectionPromptRepository;
    private final UserRepository userRepository;
    private final PromptRepository promptRepository;

    public CollectionService(
            CollectionRepository collectionRepository,
            CollectionPromptRepository collectionPromptRepository,
            UserRepository userRepository,
            PromptRepository promptRepository
    ) {
        this.collectionRepository = collectionRepository;
        this.collectionPromptRepository = collectionPromptRepository;
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> getMine(String username) {
        User owner = findUser(username);
        return collectionRepository.findByOwnerAndDeletedAtIsNullOrderByUpdatedAtDesc(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CollectionResponse create(String username, CreateCollectionRequest request) {
        User owner = findUser(username);
        OffsetDateTime now = OffsetDateTime.now();
        Collection collection = Collection.builder()
                .owner(owner)
                .name(request.name().trim())
                .slug(uniqueSlug(owner, request.name()))
                .description(normalizeOptional(request.description()))
                .coverImageUrl(normalizeOptional(request.coverImageUrl()))
                .visibility(request.visibility())
                .promptCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public CollectionResponse addPrompts(String username, UUID collectionId, List<UUID> requestedPromptIds) {
        User user = findUser(username);
        Collection collection = findOwnedCollection(collectionId, user);
        List<CollectionPrompt> currentItems = collectionPromptRepository
                .findByCollectionOrderByPositionAscCreatedAtAsc(collection);
        Set<UUID> existingIds = currentItems.stream()
                .map(item -> item.getPrompt().getId())
                .collect(java.util.stream.Collectors.toSet());
        List<UUID> promptIds = new java.util.ArrayList<>(new LinkedHashSet<>(requestedPromptIds));
        List<Prompt> prompts = promptRepository.findAllById(promptIds);
        if (prompts.size() != promptIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more prompts were not found");
        }
        java.util.Map<UUID, Prompt> promptById = prompts.stream()
                .collect(java.util.stream.Collectors.toMap(Prompt::getId, prompt -> prompt));
        int position = currentItems.stream().mapToInt(CollectionPrompt::getPosition).max().orElse(-1) + 1;
        OffsetDateTime now = OffsetDateTime.now();
        for (UUID promptId : promptIds) {
            Prompt prompt = promptById.get(promptId);
            if (!canAccess(prompt, user)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to one or more prompts");
            }
            if (existingIds.add(promptId)) {
                collectionPromptRepository.save(CollectionPrompt.builder()
                        .collection(collection)
                        .prompt(prompt)
                        .addedBy(user)
                        .position(position++)
                        .createdAt(now)
                        .build());
            }
        }
        collection.setUpdatedAt(now);
        collectionRepository.save(collection);
        collectionPromptRepository.flush();
        return toResponse(collection);
    }

    @Transactional
    public void removePrompt(String username, UUID collectionId, UUID promptId) {
        User user = findUser(username);
        Collection collection = findOwnedCollection(collectionId, user);
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt not found"));
        CollectionPrompt item = collectionPromptRepository.findByCollectionAndPrompt(collection, prompt)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt is not in this collection"));
        collectionPromptRepository.delete(item);
        collection.setUpdatedAt(OffsetDateTime.now());
        collectionRepository.save(collection);
    }

    private Collection findOwnedCollection(UUID collectionId, User owner) {
        Collection collection = collectionRepository.findById(collectionId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
        if (!Objects.equals(collection.getOwner().getId(), owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage this collection");
        }
        return collection;
    }

    private boolean canAccess(Prompt prompt, User user) {
        if (prompt.getDeletedAt() != null || prompt.getStatus() == PromptStatus.ARCHIVED) return false;
        if (Objects.equals(prompt.getAuthor().getId(), user.getId())) return true;
        return prompt.getStatus() == PromptStatus.PUBLISHED &&
                (prompt.getVisibility() == PromptVisibility.PUBLIC || prompt.getVisibility() == PromptVisibility.UNLISTED);
    }

    private User findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String uniqueSlug(User owner, String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "collection";
        if (!collectionRepository.existsByOwnerAndSlug(owner, base)) return base;
        int suffix = 2;
        while (collectionRepository.existsByOwnerAndSlug(owner, base + "-" + suffix)) suffix++;
        return base + "-" + suffix;
    }

    private CollectionResponse toResponse(Collection collection) {
        String ownerName = firstNonBlank(collection.getOwner().getDisplayName(), collection.getOwner().getUsername());
        List<java.util.UUID> promptIds = collectionPromptRepository.findByCollectionOrderByPositionAscCreatedAtAsc(collection).stream()
                .map(item -> item.getPrompt().getId())
                .toList();
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                firstNonBlank(collection.getDescription(), "No description yet."),
                collection.getCoverImageUrl(),
                visibilityLabel(collection.getVisibility()),
                ownerName,
                initials(ownerName),
                promptIds,
                0,
                0,
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }

    private String visibilityLabel(CollectionVisibility visibility) {
        return visibility == CollectionVisibility.WORKSPACE ? "Team" :
                visibility.name().substring(0, 1) + visibility.name().substring(1).toLowerCase(Locale.ROOT);
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : ""))
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "";
    }
}
