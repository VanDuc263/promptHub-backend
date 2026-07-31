package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptView;
import com.example.prompthubbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromptViewRepository extends JpaRepository<PromptView, UUID> {
    List<PromptView> findByPrompt(Prompt prompt);

    List<PromptView> findByUser(User user);
}
