package com.example.prompthubbackend;

import com.example.prompthubbackend.model.ActivityLog;
import com.example.prompthubbackend.model.Collection;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.repository.*;
import com.example.prompthubbackend.service.CollectionService;
import com.example.prompthubbackend.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CollectionActivityTests {
    private final CollectionRepository collections = mock(CollectionRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final CollectionMemberRepository members = mock(CollectionMemberRepository.class);
    private final ActivityLogRepository activities = mock(ActivityLogRepository.class);
    private final CollectionService service = new CollectionService(collections, mock(CollectionPromptRepository.class),
            users, mock(PromptRepository.class), mock(CollectionInvitationRepository.class), members, mock(MailService.class), activities);
    private final User user = User.builder().id(UUID.randomUUID()).username("reader").build();
    private final Collection collection = Collection.builder().id(UUID.randomUUID()).owner(user).build();

    @BeforeEach
    void setup() {
        when(users.findByUsernameIgnoreCase("reader")).thenReturn(Optional.of(user));
        when(collections.findById(collection.getId())).thenReturn(Optional.of(collection));
        when(activities.findByEntityTypeAndEntityIdOrderByCreatedAtDescIdDesc("collection", collection.getId(), PageRequest.of(1, 20)))
                .thenReturn(List.of(ActivityLog.builder().id(UUID.randomUUID()).actor(user).action("prompt_removed")
                        .entityName("Review code").createdAt(OffsetDateTime.now()).build()));
    }

    @Test
    void ownerReadsPaginatedHistoryWithSubject() {
        var result = service.getActivity("reader", collection.getId(), 1);
        assertEquals("reader", result.getFirst().actor());
        assertEquals("Review code", result.getFirst().subject());
    }

    @Test
    void memberCanReadHistory() {
        collection.setOwner(User.builder().id(UUID.randomUUID()).build());
        when(members.existsByCollectionAndUser(collection, user)).thenReturn(true);
        assertEquals(1, service.getActivity("reader", collection.getId(), 1).size());
    }

    @Test
    void outsiderCannotReadHistory() {
        collection.setOwner(User.builder().id(UUID.randomUUID()).build());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> service.getActivity("reader", collection.getId(), 1)).getStatusCode());
        verifyNoInteractions(activities);
    }

    @Test
    void deletedCollectionsAndNegativePagesAreRejected() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.getActivity("reader", collection.getId(), -1)).getStatusCode());
        collection.setDeletedAt(OffsetDateTime.now());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.getActivity("reader", collection.getId(), 1)).getStatusCode());
        verifyNoInteractions(activities);
    }
}
