package com.example.prompthubbackend.service;

import com.example.prompthubbackend.dto.response.WorkspaceDetailResponse;
import com.example.prompthubbackend.dto.response.WorkspaceCollectionResponse;
import com.example.prompthubbackend.dto.response.WorkspaceMemberResponse;
import com.example.prompthubbackend.dto.response.WorkspaceSummaryResponse;
import com.example.prompthubbackend.model.ActivityLog;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.WorkspaceMember;
import com.example.prompthubbackend.repository.ActivityLogRepository;
import com.example.prompthubbackend.repository.CollectionRepository;
import com.example.prompthubbackend.repository.PromptRepository;
import com.example.prompthubbackend.repository.UserRepository;
import com.example.prompthubbackend.repository.WorkspaceMemberRepository;
import com.example.prompthubbackend.repository.WorkspaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class WorkspaceService {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PromptRepository promptRepository;
    private final CollectionRepository collectionRepository;
    private final ActivityLogRepository activityLogRepository;

    public WorkspaceService(UserRepository userRepository,
                            WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository workspaceMemberRepository,
                            PromptRepository promptRepository,
                            CollectionRepository collectionRepository,
                            ActivityLogRepository activityLogRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.promptRepository = promptRepository;
        this.collectionRepository = collectionRepository;
        this.activityLogRepository = activityLogRepository;
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
                display(activity.getAction()), activity.getEntityType(), activity.getEntityName(),
                activity.getCreatedAt(), actor != null && actor.getId().equals(currentUser.getId()));
    }

    private WorkspaceMemberResponse toMember(WorkspaceMember membership, User currentUser) {
        User user = membership.getUser();
        return new WorkspaceMemberResponse(
                membership.getId(), user.getId(), displayName(user), user.getUsername(), user.getEmail(),
                user.getAvatarUrl(), display(membership.getRole().name()), membership.getJoinedAt(),
                user.getLastLoginAt(), user.getId().equals(currentUser.getId()));
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

    private String display(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
