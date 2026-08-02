package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.CreateCollectionRequest;
import com.example.prompthubbackend.dto.response.CollectionResponse;
import com.example.prompthubbackend.model.Collection;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.CollectionVisibility;
import com.example.prompthubbackend.repository.CollectionPromptRepository;
import com.example.prompthubbackend.repository.CollectionRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionPromptRepository collectionPromptRepository;
    private final UserRepository userRepository;

    public CollectionService(
            CollectionRepository collectionRepository,
            CollectionPromptRepository collectionPromptRepository,
            UserRepository userRepository
    ) {
        this.collectionRepository = collectionRepository;
        this.collectionPromptRepository = collectionPromptRepository;
        this.userRepository = userRepository;
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
        List<java.util.UUID> promptIds = collectionPromptRepository.findByCollection(collection).stream()
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
