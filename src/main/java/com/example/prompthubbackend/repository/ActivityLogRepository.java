package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.ActivityLog;
import com.example.prompthubbackend.model.User;
import com.example.prompthubbackend.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findByWorkspace(Workspace workspace);

    List<ActivityLog> findTop5ByWorkspaceOrderByCreatedAtDesc(Workspace workspace);

    List<ActivityLog> findTop100ByWorkspaceOrderByCreatedAtDesc(Workspace workspace);

    List<ActivityLog> findByActor(User actor);
}
