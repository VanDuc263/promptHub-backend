package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptReview;
import com.example.prompthubbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptReviewRepository extends JpaRepository<PromptReview, UUID> {
    List<PromptReview> findByPrompt(Prompt prompt);

    List<PromptReview> findByPromptOrderByCreatedAtDesc(Prompt prompt);

    List<PromptReview> findByUser(User user);

    Optional<PromptReview> findByPromptAndUser(Prompt prompt, User user);
}
