package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.model.ActivityLog;
import com.example.prompthubbackend.repository.ActivityLogRepository;
import com.example.prompthubbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final ActivityLogRepository activities;
    private final UserRepository users;
    public HistoryController(ActivityLogRepository activities, UserRepository users) {
        this.activities = activities;
        this.users = users;
    }
    private List<ActivityLog> owned(Authentication auth) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var user = users.findByUsernameIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return activities.findByActor(user);
    }
    public record Entry(UUID id, String action, String entityType, UUID entityId, String title, OffsetDateTime createdAt) {}
    @GetMapping
    @Transactional(readOnly = true)
    public List<Entry> list(Authentication auth) {
        return owned(auth).stream().filter(a -> !a.isHistoryHidden())
                .sorted(Comparator.comparing(ActivityLog::getCreatedAt).reversed().thenComparing(ActivityLog::getId))
                .map(a -> new Entry(a.getId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getEntityName(), a.getCreatedAt())).toList();
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void remove(@PathVariable UUID id, Authentication auth) {
        var activity = owned(auth).stream().filter(a -> a.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        activity.setHistoryHidden(true);
    }
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void clear(@RequestParam(defaultValue = "all") String mode, Authentication auth) {
        if (!Set.of("all", "viewed", "30d", "90d").contains(mode)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var cutoff = OffsetDateTime.now().minusDays(mode.equals("90d") ? 90 : 30);
        owned(auth).stream()
                .filter(a -> mode.equals("all") || (mode.equals("viewed") ? Set.of("viewed", "view").contains(a.getAction()) : a.getCreatedAt().isBefore(cutoff)))
                .forEach(a -> a.setHistoryHidden(true));
    }
}
