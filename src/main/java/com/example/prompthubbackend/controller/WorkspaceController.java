package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.response.WorkspaceDetailResponse;
import com.example.prompthubbackend.dto.response.WorkspaceCollectionResponse;
import com.example.prompthubbackend.dto.response.WorkspaceMemberResponse;
import com.example.prompthubbackend.dto.response.WorkspaceSummaryResponse;
import com.example.prompthubbackend.service.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceSummaryResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(workspaceService.getWorkspaces(authentication.getName()));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDetailResponse> detail(@PathVariable UUID workspaceId,
                                                          Authentication authentication) {
        return ResponseEntity.ok(workspaceService.getWorkspace(authentication.getName(), workspaceId));
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<WorkspaceMemberResponse>> members(@PathVariable UUID workspaceId,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(workspaceService.getMembers(authentication.getName(), workspaceId));
    }

    @GetMapping("/{workspaceId}/collections")
    public ResponseEntity<List<WorkspaceCollectionResponse>> collections(@PathVariable UUID workspaceId,
                                                                         Authentication authentication) {
        return ResponseEntity.ok(workspaceService.getCollections(authentication.getName(), workspaceId));
    }

    @GetMapping("/{workspaceId}/activities")
    public ResponseEntity<List<WorkspaceDetailResponse.Activity>> activities(@PathVariable UUID workspaceId,
                                                                             Authentication authentication) {
        return ResponseEntity.ok(workspaceService.getActivities(authentication.getName(), workspaceId));
    }
}
