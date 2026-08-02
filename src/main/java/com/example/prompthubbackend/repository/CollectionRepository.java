package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Collection;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.enums.CollectionVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    List<Collection> findByOwner(User owner);

    List<Collection> findByOwnerAndDeletedAtIsNullOrderByUpdatedAtDesc(User owner);

    List<Collection> findByWorkspace(Workspace workspace);

    long countByWorkspaceAndDeletedAtIsNull(Workspace workspace);

    Optional<Collection> findByOwnerAndSlug(User owner, String slug);

    boolean existsByOwnerAndSlug(User owner, String slug);

    List<Collection> findByVisibility(CollectionVisibility visibility);

    long countByOwnerAndVisibilityAndDeletedAtIsNull(User owner, CollectionVisibility visibility);
}
