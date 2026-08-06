package com.example.prompthubbackend.controller;

import com.example.prompthubbackend.dto.request.AcceptWorkspaceInvitationRequest;
import com.example.prompthubbackend.dto.request.InviteWorkspaceMemberRequest;
import com.example.prompthubbackend.dto.response.WorkspaceDetailResponse;
import com.example.prompthubbackend.dto.response.WorkspaceCollectionResponse;
import com.example.prompthubbackend.dto.response.WorkspaceMemberResponse;
import com.example.prompthubbackend.dto.response.WorkspaceSummaryResponse;
import com.example.prompthubbackend.dto.response.WorkspaceInvitationResponse;
import com.example.prompthubbackend.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{workspaceId}/invitations")
    public ResponseEntity<List<WorkspaceInvitationResponse>> invitations(@PathVariable UUID workspaceId,
                                                                          Authentication authentication) {
        return ResponseEntity.ok(workspaceService.getPendingInvitations(authentication.getName(), workspaceId));
    }

    @PostMapping("/{workspaceId}/invitations")
    public ResponseEntity<WorkspaceInvitationResponse> invite(@PathVariable UUID workspaceId,
                                                               @Valid @RequestBody InviteWorkspaceMemberRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.status(201).body(workspaceService.invite(authentication.getName(), workspaceId, request));
    }

    @PostMapping("/{workspaceId}/invitations/{invitationId}/resend")
    public ResponseEntity<WorkspaceInvitationResponse> resend(@PathVariable UUID workspaceId,
                                                               @PathVariable UUID invitationId,
                                                               Authentication authentication) {
        return ResponseEntity.ok(workspaceService.resendInvitation(authentication.getName(), workspaceId, invitationId));
    }

    @DeleteMapping("/{workspaceId}/invitations/{invitationId}")
    public ResponseEntity<Void> cancel(@PathVariable UUID workspaceId,
                                       @PathVariable UUID invitationId,
                                       Authentication authentication) {
        workspaceService.cancelInvitation(authentication.getName(), workspaceId, invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<WorkspaceInvitationResponse> accept(
            @Valid @RequestBody AcceptWorkspaceInvitationRequest request) {
        return ResponseEntity.ok(workspaceService.acceptInvitation(request.token()));
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
