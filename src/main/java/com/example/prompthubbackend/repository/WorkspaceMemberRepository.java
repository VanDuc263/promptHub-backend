package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.WorkspaceMember;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {
    List<WorkspaceMember> findByWorkspace(Workspace workspace);

    List<WorkspaceMember> findByUser(User user);

    long countByWorkspace(Workspace workspace);

    Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);

    List<WorkspaceMember> findByWorkspaceAndRole(Workspace workspace, WorkspaceRole role);
}
