package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Notification;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipient(User recipient);

    List<Notification> findByRecipientAndIsRead(User recipient, Boolean isRead);

    List<Notification> findByType(NotificationType type);
}
