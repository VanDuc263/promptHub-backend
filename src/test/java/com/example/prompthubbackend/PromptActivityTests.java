package com.example.prompthubbackend;

import com.example.prompthubbackend.model.ActivityLog;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.repository.*;
import com.example.prompthubbackend.service.PromptLibraryService;
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

class PromptActivityTests {
    private final PromptRepository prompts = mock(PromptRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ActivityLogRepository activities = mock(ActivityLogRepository.class);
    private final PromptLibraryService service = new PromptLibraryService(prompts,
            mock(PromptTagRepository.class), mock(PromptVersionRepository.class), users, activities);
    private final User owner = User.builder().id(UUID.randomUUID()).username("owner").build();
    private final UUID promptId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(users.findByUsernameIgnoreCase("owner")).thenReturn(Optional.of(owner));
        when(prompts.findById(promptId)).thenReturn(Optional.of(Prompt.builder().id(promptId).author(owner).build()));
    }

    @Test
    void ownerCanReadPaginatedActivityWithActorFallback() {
        when(activities.findByEntityTypeAndEntityIdOrderByCreatedAtDescIdDesc("prompt", promptId, PageRequest.of(1, 20)))
                .thenReturn(List.of(ActivityLog.builder().id(UUID.randomUUID()).actor(owner)
                        .action("updated").createdAt(OffsetDateTime.now()).build()));
        var result = service.getPromptActivity("owner", promptId, 1);
        assertEquals(1, result.size());
        assertEquals("owner", result.getFirst().actor());
        assertEquals("updated", result.getFirst().action());
    }

    @Test
    void anotherUserCannotReadActivity() {
        when(prompts.findById(promptId)).thenReturn(Optional.of(Prompt.builder().id(promptId)
                .author(User.builder().id(UUID.randomUUID()).build()).build()));
        var error = assertThrows(ResponseStatusException.class, () -> service.getPromptActivity("owner", promptId, 0));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(activities);
    }

    @Test
    void deletedPromptsAndNegativePagesAreRejected() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.getPromptActivity("owner", promptId, -1)).getStatusCode());
        when(prompts.findById(promptId)).thenReturn(Optional.of(Prompt.builder().id(promptId).author(owner)
                .deletedAt(OffsetDateTime.now()).build()));
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.getPromptActivity("owner", promptId, 0)).getStatusCode());
        verifyNoInteractions(activities);
    }
}
