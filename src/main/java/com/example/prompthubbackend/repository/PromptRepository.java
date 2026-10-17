package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prompt p where p.id = :id")
    Optional<Prompt> findByIdForUpdate(@Param("id") UUID id);

    List<Prompt> findByWorkspace(Workspace workspace);

    long countByWorkspaceAndDeletedAtIsNull(Workspace workspace);

    List<Prompt> findByAuthor(User author);

    List<Prompt> findByAuthorAndVisibilityAndStatusAndDeletedAtIsNull(
            User author,
            PromptVisibility visibility,
            PromptStatus status
    );

    Optional<Prompt> findByWorkspaceAndSlug(Workspace workspace, String slug);

    List<Prompt> findByVisibilityAndStatus(PromptVisibility visibility, PromptStatus status);
}
