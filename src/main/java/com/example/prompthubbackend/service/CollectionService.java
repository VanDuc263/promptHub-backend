package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.CreateCollectionRequest;
import com.example.prompthubbackend.dto.request.InviteCollectionMemberRequest;
import com.example.prompthubbackend.dto.response.CollectionResponse;
import com.example.prompthubbackend.dto.response.CollectionInvitationResponse;
import com.example.prompthubbackend.dto.response.CollectionMemberResponse;
import com.example.prompthubbackend.model.Collection;
import com.example.prompthubbackend.model.CollectionInvitation;
import com.example.prompthubbackend.model.CollectionMember;
import com.example.prompthubbackend.model.CollectionPrompt;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.CollectionVisibility;
import com.example.prompthubbackend.model.enums.InvitationStatus;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import com.example.prompthubbackend.repository.CollectionPromptRepository;
import com.example.prompthubbackend.repository.CollectionInvitationRepository;
import com.example.prompthubbackend.repository.CollectionMemberRepository;
import com.example.prompthubbackend.repository.CollectionRepository;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
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
    private final CollectionInvitationRepository collectionInvitationRepository;
    private final CollectionMemberRepository collectionMemberRepository;
    private final MailService mailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public CollectionService(
            CollectionRepository collectionRepository,
            CollectionPromptRepository collectionPromptRepository,
            UserRepository userRepository,
            PromptRepository promptRepository,
            CollectionInvitationRepository collectionInvitationRepository,
            CollectionMemberRepository collectionMemberRepository,
            MailService mailService
    ) {
        this.collectionRepository = collectionRepository;
        this.collectionPromptRepository = collectionPromptRepository;
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
        this.collectionInvitationRepository = collectionInvitationRepository;
        this.collectionMemberRepository = collectionMemberRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> getMine(String username) {
        User owner = findUser(username);
        java.util.LinkedHashMap<UUID, Collection> accessible = new java.util.LinkedHashMap<>();
        collectionRepository.findByOwnerAndDeletedAtIsNullOrderByUpdatedAtDesc(owner)
                .forEach(collection -> accessible.put(collection.getId(), collection));
        collectionMemberRepository.findByUserOrderByJoinedAtDesc(owner).stream()
                .map(CollectionMember::getCollection)
                .filter(collection -> collection.getDeletedAt() == null)
                .forEach(collection -> accessible.putIfAbsent(collection.getId(), collection));
        return accessible.values().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CollectionInvitationResponse invite(String username, UUID collectionId, InviteCollectionMemberRequest request) {
        User inviter = findUser(username);
        Collection collection = findOwnedCollection(collectionId, inviter);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User invitee = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No PromptHub account uses this email"));
        if (Objects.equals(inviter.getId(), invitee.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The collection owner is already a member");
        }
        if (collectionMemberRepository.existsByCollectionAndUser(collection, invitee)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user has already joined the collection");
        }

        OffsetDateTime now = OffsetDateTime.now();
        String token = UUID.randomUUID().toString() + UUID.randomUUID();
        CollectionInvitation invitation = collectionInvitationRepository.save(CollectionInvitation.builder()
                .collection(collection).inviter(inviter).inviteeEmail(email).inviteeUser(invitee)
                .token(token).status(InvitationStatus.PENDING).createdAt(now).expiresAt(now.plusSeconds(60)).build());
        String joinUrl = frontendUrl.replaceAll("/$", "") + "/collections/join?token=" + token;
        String safeCollection = HtmlUtils.htmlEscape(collection.getName());
        String safeInviter = HtmlUtils.htmlEscape(firstNonBlank(inviter.getDisplayName(), inviter.getUsername()));
        mailService.sendHtmlMail(email, "Join " + collection.getName() + " on PromptHub", """
                <div style="font-family:Arial,sans-serif;color:#111827;line-height:1.6">
                  <h2>You’re invited to join %s</h2>
                  <p>%s invited you to collaborate on this PromptHub collection.</p>
                  <p><a href="%s" style="display:inline-block;padding:12px 18px;background:#10b981;color:#07120b;text-decoration:none;border-radius:8px;font-weight:700">Join collection</a></p>
                  <p style="color:#6b7280;font-size:13px">This one-time link expires in 60 seconds.</p>
                </div>
                """.formatted(safeCollection, safeInviter, HtmlUtils.htmlEscape(joinUrl)));
        return new CollectionInvitationResponse(collection.getId(), collection.getName(), email, invitation.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public List<CollectionMemberResponse> getMembers(String username, UUID collectionId) {
        User currentUser = findUser(username);
        Collection collection = collectionRepository.findById(collectionId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
        boolean owner = Objects.equals(collection.getOwner().getId(), currentUser.getId());
        if (!owner && !collectionMemberRepository.existsByCollectionAndUser(collection, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to collection members");
        }
        java.util.ArrayList<CollectionMemberResponse> members = new java.util.ArrayList<>();
        User collectionOwner = collection.getOwner();
        members.add(memberResponse(collectionOwner, "Owner", collection.getCreatedAt(), currentUser));
        collectionMemberRepository.findByCollectionOrderByJoinedAtAsc(collection).forEach(member ->
                members.add(memberResponse(member.getUser(), "Member", member.getJoinedAt(), currentUser)));
        return members;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public CollectionInvitationResponse acceptInvitation(String token) {
        CollectionInvitation invitation = collectionInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation link is invalid"));
        OffsetDateTime now = OffsetDateTime.now();
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation link has already been used");
        }
        if (!now.isBefore(invitation.getExpiresAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            collectionInvitationRepository.save(invitation);
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation link expired after 60 seconds");
        }
        Collection collection = invitation.getCollection();
        if (collection.getDeletedAt() != null) throw new ResponseStatusException(HttpStatus.GONE, "Collection is no longer available");
        if (!collectionMemberRepository.existsByCollectionAndUser(collection, invitation.getInviteeUser())) {
            collectionMemberRepository.save(CollectionMember.builder().collection(collection)
                    .user(invitation.getInviteeUser()).invitedBy(invitation.getInviter()).joinedAt(now).build());
        }
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(now);
        collectionInvitationRepository.save(invitation);
        return new CollectionInvitationResponse(collection.getId(), collection.getName(), invitation.getInviteeEmail(), invitation.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> getPublic() {
        return collectionRepository
                .findByVisibilityAndDeletedAtIsNullOrderByUpdatedAtDesc(CollectionVisibility.PUBLIC)
                .stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CollectionResponse getPublic(UUID collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .filter(item -> item.getDeletedAt() == null && item.getVisibility() == CollectionVisibility.PUBLIC)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Public collection not found"));
        return toPublicResponse(collection);
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
        return toResponse(collection, false);
    }

    private CollectionResponse toPublicResponse(Collection collection) {
        return toResponse(collection, true);
    }

    private CollectionResponse toResponse(Collection collection, boolean publicPromptsOnly) {
        String ownerName = firstNonBlank(collection.getOwner().getDisplayName(), collection.getOwner().getUsername());
        List<java.util.UUID> promptIds = collectionPromptRepository.findByCollectionOrderByPositionAscCreatedAtAsc(collection).stream()
                .map(CollectionPrompt::getPrompt)
                .filter(prompt -> !publicPromptsOnly || isPubliclyDiscoverable(prompt))
                .map(Prompt::getId)
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

    private boolean isPubliclyDiscoverable(Prompt prompt) {
        return prompt.getDeletedAt() == null
                && prompt.getStatus() == PromptStatus.PUBLISHED
                && prompt.getVisibility() == PromptVisibility.PUBLIC;
    }

    private CollectionMemberResponse memberResponse(User user, String role, OffsetDateTime joinedAt, User currentUser) {
        String name = firstNonBlank(user.getDisplayName(), user.getUsername());
        return new CollectionMemberResponse(user.getId(), name, initials(name), user.getEmail(), role,
                joinedAt, user.getLastLoginAt(), Objects.equals(user.getId(), currentUser.getId()));
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
