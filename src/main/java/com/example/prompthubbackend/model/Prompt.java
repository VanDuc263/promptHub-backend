package com.example.prompthubbackend.model;

import com.example.prompthubbackend.model.enums.PromptStatus;
import com.example.prompthubbackend.model.enums.PromptVisibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prompts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prompt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "slug", nullable = false, length = 220)
    private String slug;

    @Column(name = "short_description", length = 300)
    private String shortDescription;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private PromptVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromptStatus status;

    @Column(name = "current_version_id")
    private UUID currentVersionId;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "model_provider", length = 50)
    private String modelProvider;

    @Column(name = "recommended_model", length = 100)
    private String recommendedModel;

    @Column(name = "language", length = 20)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forked_from_prompt_id")
    private Prompt forkedFromPrompt;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "run_count", nullable = false)
    private Long runCount = 0L;

    @Column(name = "favorite_count", nullable = false)
    private Long favoriteCount = 0L;

    @Column(name = "fork_count", nullable = false)
    private Long forkCount = 0L;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
