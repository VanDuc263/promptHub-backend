package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptVersionRepository extends JpaRepository<PromptVersion, UUID> {
    List<PromptVersion> findByPromptOrderByVersionNumberDesc(Prompt prompt);

    Optional<PromptVersion> findFirstByPromptOrderByVersionNumberDesc(Prompt prompt);

    Optional<PromptVersion> findByPromptAndVersionNumber(Prompt prompt, Integer versionNumber);
}
