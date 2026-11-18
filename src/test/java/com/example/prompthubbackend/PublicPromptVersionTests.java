package com.example.prompthubbackend;

import com.example.prompthubbackend.model.*;
import com.example.prompthubbackend.model.enums.*;
import com.example.prompthubbackend.repository.*;
import com.example.prompthubbackend.service.PromptExploreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicPromptVersionTests {
    private final PromptRepository prompts = mock(PromptRepository.class);
    private final PromptVersionRepository versions = mock(PromptVersionRepository.class);
    private final PromptExploreService service = new PromptExploreService(prompts, mock(PromptTagRepository.class),
            versions, mock(PromptReviewRepository.class), new ObjectMapper());
    private final User author = User.builder().username("creator").displayName("Creator").build();
    private final Prompt prompt = Prompt.builder().id(UUID.randomUUID()).visibility(PromptVisibility.PUBLIC)
            .status(PromptStatus.PUBLISHED).build();

    @Test
    void returnsCurrentAndOlderSnapshotsOnly() {
        var old = version(1, "Old prompt", "Old system");
        old.setExamples("[{\"id\":\"old-example\",\"title\":\"Old example\",\"input\":\"Old input\",\"output\":\"Old output\"}]");
        old.setDocumentation("[{\"title\":\"Usage\",\"content\":\"Old instructions\"}]");
        var current = version(2, "Current prompt", "Current system");
        var future = version(3, "Future draft", "");
        prompt.setCurrentVersionId(current.getId());
        when(prompts.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        when(versions.findById(current.getId())).thenReturn(Optional.of(current));
        when(versions.findByPromptOrderByVersionNumberDesc(prompt)).thenReturn(List.of(future, current, old));
        var result = service.getPublicVersions(prompt.getId());
        assertEquals(List.of("v2", "v1"), result.stream().map(item -> item.version()).toList());
        assertTrue(result.getFirst().current());
        assertFalse(result.getLast().current());
        assertEquals("Old prompt", result.getLast().content());
        assertEquals("Old system", result.getLast().systemMessage());
        assertEquals("old-example", result.getLast().examples().getFirst().id());
        assertEquals("Old instructions", result.getLast().documentation().getFirst().content());
        verify(versions, never()).save(any());
    }

    @Test
    void rejectsPrivateUnlistedDraftAndDeletedPrompts() {
        when(prompts.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        prompt.setVisibility(PromptVisibility.PRIVATE);
        assertThrows(ResponseStatusException.class, () -> service.getPublicVersions(prompt.getId()));
        prompt.setVisibility(PromptVisibility.UNLISTED);
        assertThrows(ResponseStatusException.class, () -> service.getPublicVersions(prompt.getId()));
        prompt.setVisibility(PromptVisibility.PUBLIC);
        prompt.setStatus(PromptStatus.DRAFT);
        assertThrows(ResponseStatusException.class, () -> service.getPublicVersions(prompt.getId()));
        prompt.setStatus(PromptStatus.PUBLISHED);
        prompt.setDeletedAt(java.time.OffsetDateTime.now());
        assertThrows(ResponseStatusException.class, () -> service.getPublicVersions(prompt.getId()));
        verifyNoInteractions(versions);
    }

    @Test
    void missingCurrentVersionHasNoPublicHistory() {
        when(prompts.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        assertTrue(service.getPublicVersions(prompt.getId()).isEmpty());
        verifyNoInteractions(versions);
    }

    private PromptVersion version(int number, String content, String system) {
        return PromptVersion.builder().id(UUID.randomUUID()).prompt(prompt).versionNumber(number)
                .content(content).systemMessage(system).createdBy(author).variables("[]").modelConfig("{}").build();
    }
}
