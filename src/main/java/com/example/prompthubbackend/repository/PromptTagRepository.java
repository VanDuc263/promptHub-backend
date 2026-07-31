package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptTag;
import com.example.prompthubbackend.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptTagRepository extends JpaRepository<PromptTag, PromptTag.PromptTagId> {
    List<PromptTag> findByPrompt(Prompt prompt);

    List<PromptTag> findByTag(Tag tag);

    Optional<PromptTag> findByPromptAndTag(Prompt prompt, Tag tag);
}
