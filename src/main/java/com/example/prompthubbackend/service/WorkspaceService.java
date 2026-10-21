package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.request.InviteWorkspaceMemberRequest;
import com.example.prompthubbackend.dto.response.WorkspaceDetailResponse;
import com.example.prompthubbackend.dto.response.WorkspaceCollectionResponse;
import com.example.prompthubbackend.dto.response.WorkspaceMemberResponse;
import com.example.prompthubbackend.dto.response.WorkspaceSummaryResponse;
import com.example.prompthubbackend.dto.response.WorkspaceInvitationResponse;
import com.example.prompthubbackend.model.ActivityLog;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.WorkspaceMember;
import com.example.prompthubbackend.model.WorkspaceInvitation;
import com.example.prompthubbackend.model.enums.InvitationStatus;
import com.example.prompthubbackend.model.enums.WorkspaceRole;
import com.example.prompthubbackend.repository.ActivityLogRepository;
import com.example.prompthubbackend.repository.CollectionRepository;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.UserRepository;
import com.example.prompthubbackend.repository.WorkspaceMemberRepository;
import com.example.prompthubbackend.repository.WorkspaceInvitationRepository;
import com.example.prompthubbackend.repository.WorkspaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class WorkspaceService {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PromptRepository promptRepository;
    private final CollectionRepository collectionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;
    private final MailService mailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public WorkspaceService(UserRepository userRepository,
                            WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository workspaceMemberRepository,
                            PromptRepository promptRepository,
                            CollectionRepository collectionRepository,
                            ActivityLogRepository activityLogRepository,
                            WorkspaceInvitationRepository workspaceInvitationRepository,
                            MailService mailService) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.promptRepository = promptRepository;
        this.collectionRepository = collectionRepository;
        this.activityLogRepository = activityLogRepository;
        this.workspaceInvitationRepository = workspaceInvitationRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceSummaryResponse> getWorkspaces(String username) {
        User user = requireUser(username);
        return workspaceMemberRepository.findByUser(user).stream()
                .filter(member -> member.getWorkspace().getDeletedAt() == null)
                .sorted(Comparator
                        .comparing((WorkspaceMember member) -> !Boolean.TRUE.equals(member.getWorkspace().getIsPersonal()))
                        .thenComparing(member -> member.getWorkspace().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceDetailResponse getWorkspace(String username, UUID workspaceId) {
        User user = requireUser(username);
        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceAndUser(
                        requireWorkspace(workspaceId), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        Workspace workspace = membership.getWorkspace();

        List<WorkspaceDetailResponse.Activity> activities = activityLogRepository
                .findTop5ByWorkspaceOrderByCreatedAtDesc(workspace).stream()
                .map(activity -> toActivity(activity, user))
                .toList();

        return new WorkspaceDetailResponse(
                workspace.getId(), workspace.getName(), workspace.getSlug(), workspace.getDescription(),
                workspace.getAvatarUrl(), Boolean.TRUE.equals(workspace.getIsPersonal()), display(membership.getRole().name()),
                displayName(workspace.getOwner()), workspace.getCreatedAt(),
                new WorkspaceDetailResponse.Overview(
                        workspaceMemberRepository.countByWorkspace(workspace),
                        promptRepository.countByWorkspaceAndDeletedAtIsNull(workspace),
                        collectionRepository.countByWorkspaceAndDeletedAtIsNull(workspace)),
                activities);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(String username, UUID workspaceId) {
        User currentUser = requireUser(username);
        Workspace workspace = requireWorkspace(workspaceId);
        workspaceMemberRepository.findByWorkspaceAndUser(workspace, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        return workspaceMemberRepository.findByWorkspace(workspace).stream()
                .sorted(Comparator
                        .comparingInt((WorkspaceMember member) -> member.getRole().ordinal())
                        .thenComparing(member -> displayName(member.getUser()), String.CASE_INSENSITIVE_ORDER))
                .map(member -> toMember(member, currentUser))
                .toList();
    }

    @Transactional
    public List<WorkspaceInvitationResponse> getPendingInvitations(String username, UUID workspaceId) {
        User manager = requireUser(username);
        Workspace workspace = requireManagedWorkspace(workspaceId, manager);
        OffsetDateTime now = OffsetDateTime.now();
        return workspaceInvitationRepository
                .findByWorkspaceAndStatusOrderByCreatedAtDesc(workspace, InvitationStatus.PENDING).stream()
                .filter(invitation -> {
                    if (now.isBefore(invitation.getExpiresAt())) return true;
                    invitation.setStatus(InvitationStatus.EXPIRED);
                    workspaceInvitationRepository.save(invitation);
                    return false;
                })
                .map(this::toInvitation)
                .toList();
    }

    @Transactional
    public WorkspaceInvitationResponse invite(String username, UUID workspaceId, InviteWorkspaceMemberRequest request) {
        User inviter = requireUser(username);
        Workspace workspace = requireManagedWorkspace(workspaceId, inviter);
        if (request.role() == WorkspaceRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot be assigned through an invitation");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User invitee = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No PromptHub account uses this email"));
        if (workspaceMemberRepository.findByWorkspaceAndUser(workspace, invitee).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user is already a workspace member");
        }
        workspaceInvitationRepository.findByWorkspaceAndInviteeEmailIgnoreCaseAndStatus(
                        workspace, email, InvitationStatus.PENDING)
                .ifPresent(existing -> {
                    if (OffsetDateTime.now().isBefore(existing.getExpiresAt())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending invitation already exists for this email");
                    }
                    existing.setStatus(InvitationStatus.EXPIRED);
                    workspaceInvitationRepository.save(existing);
                });

        OffsetDateTime now = OffsetDateTime.now();
        WorkspaceInvitation invitation = workspaceInvitationRepository.save(WorkspaceInvitation.builder()
                .workspace(workspace)
                .inviter(inviter)
                .inviteeEmail(email)
                .inviteeUser(invitee)
                .role(request.role())
                .token(newToken())
                .status(InvitationStatus.PENDING)
                .expiresAt(now.plusHours(24))
                .createdAt(now)
                .build());
        activityLogRepository.save(ActivityLog.builder()
                .workspace(workspace).actor(inviter).action("MEMBER_INVITED")
                .entityType("WORKSPACE_MEMBER").entityId(invitation.getId()).entityName(email)
                .newValues("{\"role\":\"" + request.role().name() + "\"}").createdAt(now).build());
        // Force database errors to surface before the non-transactional email side effect.
        workspaceInvitationRepository.flush();
        activityLogRepository.flush();
        sendInvitationMail(invitation, request.message());
        return toInvitation(invitation);
    }

    @Transactional
    public WorkspaceInvitationResponse resendInvitation(String username, UUID workspaceId, UUID invitationId) {
        User manager = requireUser(username);
        Workspace workspace = requireManagedWorkspace(workspaceId, manager);
        WorkspaceInvitation invitation = requireInvitation(workspace, invitationId);
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending invitations can be resent");
        }
        OffsetDateTime now = OffsetDateTime.now();
        invitation.setToken(newToken());
        invitation.setCreatedAt(now);
        invitation.setExpiresAt(now.plusHours(24));
        invitation.setInviter(manager);
        workspaceInvitationRepository.save(invitation);
        workspaceInvitationRepository.flush();
        sendInvitationMail(invitation, null);
        return toInvitation(invitation);
    }

    @Transactional
    public void cancelInvitation(String username, UUID workspaceId, UUID invitationId) {
        User manager = requireUser(username);
        WorkspaceInvitation invitation = requireInvitation(requireManagedWorkspace(workspaceId, manager), invitationId);
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending invitations can be cancelled");
        }
        invitation.setStatus(InvitationStatus.CANCELLED);
        workspaceInvitationRepository.save(invitation);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public WorkspaceInvitationResponse acceptInvitation(String token) {
        WorkspaceInvitation invitation = workspaceInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation link is invalid"));
        OffsetDateTime now = OffsetDateTime.now();
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation link has already been used or cancelled");
        }
        if (!now.isBefore(invitation.getExpiresAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            workspaceInvitationRepository.save(invitation);
            throw new ResponseStatusException(HttpStatus.GONE, "Invitation link has expired");
        }
        Workspace workspace = invitation.getWorkspace();
        User invitee = invitation.getInviteeUser();
        if (workspace.getDeletedAt() != null || invitee == null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Workspace invitation is no longer available");
        }
        if (workspaceMemberRepository.findByWorkspaceAndUser(workspace, invitee).isEmpty()) {
            workspaceMemberRepository.save(WorkspaceMember.builder()
                    .workspace(workspace).user(invitee).role(invitation.getRole())
                    .joinedAt(now).updatedAt(now).build());
            activityLogRepository.save(ActivityLog.builder()
                    .workspace(workspace).actor(invitee).action("MEMBER_JOINED")
                    .entityType("WORKSPACE_MEMBER").entityName(displayName(invitee)).createdAt(now).build());
        }
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(now);
        workspaceInvitationRepository.save(invitation);
        return toInvitation(invitation);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceCollectionResponse> getCollections(String username, UUID workspaceId) {
        User currentUser = requireUser(username);
        Workspace workspace = requireWorkspace(workspaceId);
        workspaceMemberRepository.findByWorkspaceAndUser(workspace, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        return collectionRepository.findByWorkspaceAndDeletedAtIsNullOrderByUpdatedAtDesc(workspace).stream()
                .map(collection -> {
                    String ownerName = displayName(collection.getOwner());
                    return new WorkspaceCollectionResponse(
                            collection.getId(), collection.getName(),
                            collection.getDescription() == null || collection.getDescription().isBlank()
                                    ? "No description yet." : collection.getDescription(),
                            collection.getCoverImageUrl(), display(collection.getVisibility().name()),
                            collection.getPromptCount(), initials(ownerName), collection.getUpdatedAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkspaceDetailResponse.Activity> getActivities(String username, UUID workspaceId) {
        User currentUser = requireUser(username);
        Workspace workspace = requireWorkspace(workspaceId);
        workspaceMemberRepository.findByWorkspaceAndUser(workspace, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        return activityLogRepository.findTop100ByWorkspaceOrderByCreatedAtDesc(workspace).stream()
                .map(activity -> toActivity(activity, currentUser))
                .toList();
    }

    private WorkspaceSummaryResponse toSummary(WorkspaceMember membership) {
        Workspace workspace = membership.getWorkspace();
        return new WorkspaceSummaryResponse(
                workspace.getId(), workspace.getName(), workspace.getSlug(), workspace.getDescription(),
                workspace.getAvatarUrl(), Boolean.TRUE.equals(workspace.getIsPersonal()), display(membership.getRole().name()),
                promptRepository.countByWorkspaceAndDeletedAtIsNull(workspace),
                collectionRepository.countByWorkspaceAndDeletedAtIsNull(workspace),
                workspaceMemberRepository.countByWorkspace(workspace));
    }

    private WorkspaceDetailResponse.Activity toActivity(ActivityLog activity, User currentUser) {
        User actor = activity.getActor();
        return new WorkspaceDetailResponse.Activity(
                activity.getId(), actor == null ? "PromptHub" : displayName(actor),
                activityAction(activity.getAction()), activity.getEntityType(), activity.getEntityName(),
                activity.getCreatedAt(), actor != null && actor.getId().equals(currentUser.getId()),
                activity.getOldValues(), activity.getNewValues());
    }

    private WorkspaceMemberResponse toMember(WorkspaceMember membership, User currentUser) {
        User user = membership.getUser();
        return new WorkspaceMemberResponse(
                membership.getId(), user.getId(), displayName(user), user.getUsername(), user.getEmail(),
                user.getAvatarUrl(), display(membership.getRole().name()), membership.getJoinedAt(),
                user.getLastLoginAt(), user.getId().equals(currentUser.getId()));
    }

    @Transactional
    public void saveCollection(String username, UUID workspaceId, UUID collectionId,
            com.example.prompthubbackend.dto.request.CreateCollectionRequest request) {
        User user = requireUser(username);
        Workspace workspace = requireCollectionWorkspace(workspaceId, user);
        var collection = collectionId == null ? new com.example.prompthubbackend.model.Collection()
                : requireCollection(workspace, collectionId);
        if (collectionId == null) {
            collection.setOwner(user);
            collection.setWorkspace(workspace);
            collection.setSlug("collection-" + UUID.randomUUID());
            collection.setPromptCount(0);
            collection.setCreatedAt(OffsetDateTime.now());
        }
        collection.setName(request.name().trim());
        collection.setDescription(request.description() == null ? null : request.description().trim());
        collection.setVisibility(request.visibility());
        collection.setCoverImageUrl(request.coverImageUrl());
        collection.setUpdatedAt(OffsetDateTime.now());
        collectionRepository.save(collection);
        logCollection(workspace, user, collection, collectionId == null ? "COLLECTION_CREATED" : "COLLECTION_UPDATED");
    }

    @Transactional
    public void deleteCollection(String username, UUID workspaceId, UUID collectionId) {
        User user = requireUser(username);
        Workspace workspace = requireCollectionWorkspace(workspaceId, user);
        var collection = requireCollection(workspace, collectionId);
        collection.setDeletedAt(OffsetDateTime.now());
        collection.setUpdatedAt(OffsetDateTime.now());
        collectionRepository.save(collection);
        logCollection(workspace, user, collection, "COLLECTION_DELETED");
    }

    private Workspace requireCollectionWorkspace(UUID id, User user) {
        Workspace workspace = requireWorkspace(id);
        var member = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        if (member.getRole() == WorkspaceRole.VIEWER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Viewers cannot manage collections");
        return workspace;
    }

    private com.example.prompthubbackend.model.Collection requireCollection(Workspace workspace, UUID id) {
        return collectionRepository.findById(id)
                .filter(item -> item.getDeletedAt() == null && item.getWorkspace() != null
                        && item.getWorkspace().getId().equals(workspace.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
    }

    private void logCollection(Workspace workspace, User user,
            com.example.prompthubbackend.model.Collection collection, String action) {
        activityLogRepository.save(ActivityLog.builder().workspace(workspace).actor(user)
                .entityType("collection").entityId(collection.getId()).entityName(collection.getName())
                .action(action).createdAt(OffsetDateTime.now()).build());
    }

    private Workspace requireManagedWorkspace(UUID workspaceId, User user) {
        Workspace workspace = requireWorkspace(workspaceId);
        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        if (membership.getRole() != WorkspaceRole.OWNER && membership.getRole() != WorkspaceRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workspace owners and admins can manage invitations");
        }
        return workspace;
    }

    private WorkspaceInvitation requireInvitation(Workspace workspace, UUID invitationId) {
        return workspaceInvitationRepository.findById(invitationId)
                .filter(invitation -> Objects.equals(invitation.getWorkspace().getId(), workspace.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace invitation not found"));
    }

    private void sendInvitationMail(WorkspaceInvitation invitation, String message) {
        String joinUrl = frontendUrl.replaceAll("/$", "") + "/workspaces/join?token=" + invitation.getToken();
        String safeWorkspace = HtmlUtils.htmlEscape(invitation.getWorkspace().getName());
        String safeInviter = HtmlUtils.htmlEscape(displayName(invitation.getInviter()));
        String optionalMessage = message == null || message.isBlank() ? "" :
                "<blockquote style=\"margin:16px 0;padding:12px 16px;border-left:3px solid #8b5cf6;background:#f5f3ff;color:#4b5563\">"
                        + HtmlUtils.htmlEscape(message.trim()) + "</blockquote>";
        mailService.sendHtmlMail(invitation.getInviteeEmail(),
                "Join " + invitation.getWorkspace().getName() + " on PromptHub", """
                <div style="font-family:Arial,sans-serif;color:#111827;line-height:1.6">
                  <h2>You’re invited to join %s</h2>
                  <p>%s invited you to collaborate as <strong>%s</strong>.</p>
                  %s
                  <p><a href="%s" style="display:inline-block;padding:12px 18px;background:#8b5cf6;color:#fff;text-decoration:none;border-radius:8px;font-weight:700">Join workspace</a></p>
                  <p style="color:#6b7280;font-size:13px">This secure, one-time link expires in 24 hours.</p>
                </div>
                """.formatted(safeWorkspace, safeInviter, display(invitation.getRole().name()),
                        optionalMessage, HtmlUtils.htmlEscape(joinUrl)));
    }

    private WorkspaceInvitationResponse toInvitation(WorkspaceInvitation invitation) {
        return new WorkspaceInvitationResponse(
                invitation.getId(), invitation.getWorkspace().getId(), invitation.getWorkspace().getName(),
                invitation.getInviteeEmail(), display(invitation.getRole().name()),
                display(invitation.getStatus().name()), invitation.getExpiresAt(), invitation.getCreatedAt());
    }

    private String newToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID();
    }

    private Workspace requireWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .filter(workspace -> workspace.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
    }

    private User requireUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String displayName(User user) {
        return user.getDisplayName() == null || user.getDisplayName().isBlank() ? user.getUsername() : user.getDisplayName();
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : ""))
                .toUpperCase(Locale.ROOT);
    }

    private String activityAction(String action) {
        return switch (action.toUpperCase(Locale.ROOT)) {
            case "PROMPT_CREATED" -> "Created";
            case "PROMPT_PUBLISHED" -> "Published";
            case "PROMPT_UPDATED" -> "Updated";
            case "MEMBER_INVITED" -> "Invited";
            case "MEMBER_JOINED" -> "Added";
            case "MEMBER_ROLE_CHANGED" -> "Changed the role of";
            case "COLLECTION_CREATED" -> "Created collection";
            case "COLLECTION_UPDATED" -> "Updated collection";
            case "COMMENT_CREATED" -> "Commented on";
            case "VERSION_RESTORED" -> "Restored a version of";
            default -> display(action);
        };
    }

    private String display(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
