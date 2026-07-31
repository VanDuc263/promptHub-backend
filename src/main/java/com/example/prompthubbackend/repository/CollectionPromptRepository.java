package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Collection;
import com.example.prompthubbackend.model.CollectionPrompt;
import com.example.prompthubbackend.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionPromptRepository extends JpaRepository<CollectionPrompt, CollectionPrompt.CollectionPromptId> {
    List<CollectionPrompt> findByCollection(Collection collection);

    List<CollectionPrompt> findByPrompt(Prompt prompt);

    Optional<CollectionPrompt> findByCollectionAndPrompt(Collection collection, Prompt prompt);
}
