package com.example.prompthubbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prompt_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptView {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "ip_hash", length = 255)
    private String ipHash;

    @Column(name = "viewed_at", nullable = false)
    private OffsetDateTime viewedAt;
}
