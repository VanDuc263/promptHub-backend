package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Favorite;
import com.example.prompthubbackend.model.Prompt;
import com.example.prompthubbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Favorite.FavoriteId> {
    List<Favorite> findByUser(User user);

    List<Favorite> findByPrompt(Prompt prompt);

    Optional<Favorite> findByUserAndPrompt(User user, Prompt prompt);
}
