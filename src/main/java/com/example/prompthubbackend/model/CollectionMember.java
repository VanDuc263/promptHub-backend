package com.example.prompthubbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "collection_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(CollectionMember.CollectionMemberId.class)
public class CollectionMember {
    @Id @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;
    @Id @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;
    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CollectionMemberId implements Serializable {
        private UUID collection;
        private UUID user;
    }
}
