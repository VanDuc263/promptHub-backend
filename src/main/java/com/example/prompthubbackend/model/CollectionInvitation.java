package com.example.prompthubbackend.model;

import com.example.prompthubbackend.model.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "collection_invitations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionInvitation {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) @Column(columnDefinition = "uuid")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;
    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "invitee_user_id", nullable = false)
    private User inviteeUser;
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.NAMED_ENUM) @Column(name = "status", nullable = false)
    private InvitationStatus status;
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
