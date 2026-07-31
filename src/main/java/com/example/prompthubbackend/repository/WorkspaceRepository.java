package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import com.example.prompthubbackend.model.User;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findBySlug(String slug);
    Optional<Workspace> findFirstByOwnerAndIsPersonalTrue(User owner);
}
