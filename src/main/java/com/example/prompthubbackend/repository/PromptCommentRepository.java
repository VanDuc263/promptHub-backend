package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptComment;
import com.example.prompthubbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromptCommentRepository extends JpaRepository<PromptComment, UUID> {
    List<PromptComment> findByPrompt(Prompt prompt);

    List<PromptComment> findByPromptAndDeletedAtIsNullOrderByCreatedAtAsc(Prompt prompt);

    List<PromptComment> findByUser(User user);

    List<PromptComment> findByParent(PromptComment parent);
}
