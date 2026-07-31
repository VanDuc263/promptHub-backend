package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.PromptRun;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import com.example.prompthubbackend.model.enums.RunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromptRunRepository extends JpaRepository<PromptRun, UUID> {
    List<PromptRun> findByUser(User user);

    List<PromptRun> findByPrompt(Prompt prompt);

    List<PromptRun> findByWorkspace(Workspace workspace);

    List<PromptRun> findByStatus(RunStatus status);
}
