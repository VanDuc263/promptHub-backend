package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.CollectionInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface CollectionInvitationRepository extends JpaRepository<CollectionInvitation, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CollectionInvitation> findByToken(String token);
}
