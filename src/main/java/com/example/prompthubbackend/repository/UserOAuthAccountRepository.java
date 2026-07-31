package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.UserOAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, UUID> {
    List<UserOAuthAccount> findByUser(User user);

    Optional<UserOAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);
}
