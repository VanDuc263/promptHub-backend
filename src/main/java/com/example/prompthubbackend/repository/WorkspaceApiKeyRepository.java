package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.WorkspaceApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceApiKeyRepository extends JpaRepository<WorkspaceApiKey, UUID> {
    List<WorkspaceApiKey> findByWorkspace(Workspace workspace);

    Optional<WorkspaceApiKey> findByKeyHash(String keyHash);
}
