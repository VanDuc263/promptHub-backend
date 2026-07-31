package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.WorkspaceInvitation;
import com.example.prompthubbackend.model.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {
    List<WorkspaceInvitation> findByWorkspace(Workspace workspace);

    Optional<WorkspaceInvitation> findByToken(String token);

    List<WorkspaceInvitation> findByStatus(InvitationStatus status);
}
