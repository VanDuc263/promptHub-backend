package com.example.prompthubbackend;

import com.example.prompthubbackend.controller.HistoryController;
import com.example.prompthubbackend.model.ActivityLog;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.repository.ActivityLogRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistoryTests {
    @Test
    void historyIsScopedToActorAndRemovalPreservesAudit() {
        var logs = mock(ActivityLogRepository.class);
        var users = mock(UserRepository.class);
        var user = new User();
        var auth = new UsernamePasswordAuthenticationToken("alice", null);
        when(users.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        var older = ActivityLog.builder().id(UUID.randomUUID()).action("created").entityType("prompt")
                .createdAt(OffsetDateTime.now().minusDays(40)).build();
        var newer = ActivityLog.builder().id(UUID.randomUUID()).action("updated").entityType("prompt")
                .createdAt(OffsetDateTime.now()).build();
        when(logs.findByActor(user)).thenReturn(List.of(older, newer));
        var controller = new HistoryController(logs, users);
        assertEquals(newer.getId(), controller.list(auth).get(0).id());
        controller.clear("30d", auth);
        assertTrue(older.isHistoryHidden());
        assertFalse(newer.isHistoryHidden());
        assertEquals(1, controller.list(auth).size());
        assertThrows(ResponseStatusException.class, () -> controller.remove(UUID.randomUUID(), auth));
        controller.remove(newer.getId(), auth);
        assertTrue(controller.list(auth).isEmpty());
        verify(logs, never()).delete(any());
        assertThrows(ResponseStatusException.class, () -> controller.clear("invalid", auth));
        assertThrows(ResponseStatusException.class, () -> controller.list(null));
    }
}
