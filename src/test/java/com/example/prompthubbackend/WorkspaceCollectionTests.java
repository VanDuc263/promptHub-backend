package com.example.prompthubbackend;

import com.example.prompthubbackend.dto.request.CreateCollectionRequest;
import com.example.prompthubbackend.model.*;
import com.example.prompthubbackend.model.enums.*;
import com.example.prompthubbackend.repository.*;
import com.example.prompthubbackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkspaceCollectionTests {
    private final UserRepository users = mock(UserRepository.class);
    private final WorkspaceRepository workspaces = mock(WorkspaceRepository.class);
    private final WorkspaceMemberRepository members = mock(WorkspaceMemberRepository.class);
    private final CollectionRepository collections = mock(CollectionRepository.class);
    private final ActivityLogRepository activities = mock(ActivityLogRepository.class);
    private final WorkspaceService service = new WorkspaceService(users, workspaces, members,
            mock(PromptRepository.class), collections, activities, mock(WorkspaceInvitationRepository.class), mock(MailService.class));
    private final User user = User.builder().id(UUID.randomUUID()).username("editor").build();
    private final Workspace workspace = Workspace.builder().id(UUID.randomUUID()).build();
    private final WorkspaceMember member = WorkspaceMember.builder().workspace(workspace).user(user).role(WorkspaceRole.EDITOR).build();
    private final CreateCollectionRequest request = new CreateCollectionRequest(" Design ", "Team prompts", null, CollectionVisibility.WORKSPACE, workspace.getId());

    @BeforeEach
    void setup() {
        when(users.findByUsernameIgnoreCase("editor")).thenReturn(Optional.of(user));
        when(workspaces.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(members.findByWorkspaceAndUser(workspace, user)).thenReturn(Optional.of(member));
    }

    @Test
    void editorCreatesCollectionInWorkspaceAndLogsActivity() {
        service.saveCollection("editor", workspace.getId(), null, request);
        verify(collections).save(argThat(item -> item.getWorkspace() == workspace && item.getOwner() == user
                && item.getName().equals("Design") && item.getCreatedAt() != null));
        verify(activities).save(argThat(log -> log.getWorkspace() == workspace && log.getAction().equals("COLLECTION_CREATED")));
    }

    @Test
    void viewerCannotCreateOrDelete() {
        member.setRole(WorkspaceRole.VIEWER);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> service.saveCollection("editor", workspace.getId(), null, request)).getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.deleteCollection("editor", workspace.getId(), UUID.randomUUID()));
        verifyNoInteractions(collections, activities);
    }

    @Test
    void collectionFromAnotherWorkspaceCannotBeChanged() {
        var collection = Collection.builder().id(UUID.randomUUID()).workspace(Workspace.builder().id(UUID.randomUUID()).build()).build();
        when(collections.findById(collection.getId())).thenReturn(Optional.of(collection));
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.saveCollection("editor", workspace.getId(), collection.getId(), request)).getStatusCode());
        assertThrows(ResponseStatusException.class, () -> service.deleteCollection("editor", workspace.getId(), collection.getId()));
        verify(collections, never()).save(any());
    }

    @Test
    void deletionKeepsCollectionAndPromptsForSoftDelete() {
        var collection = Collection.builder().id(UUID.randomUUID()).workspace(workspace).name("Design").build();
        when(collections.findById(collection.getId())).thenReturn(Optional.of(collection));
        service.deleteCollection("editor", workspace.getId(), collection.getId());
        assertNotNull(collection.getDeletedAt());
        verify(collections).save(collection);
        verify(collections, never()).delete(any());
    }
}
