package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, UUID> {
    List<Prompt> findByWorkspace(Workspace workspace);

    List<Prompt> findByAuthor(User author);

    Optional<Prompt> findByWorkspaceAndSlug(Workspace workspace, String slug);

    List<Prompt> findByVisibilityAndStatus(PromptVisibility visibility, PromptStatus status);
}
