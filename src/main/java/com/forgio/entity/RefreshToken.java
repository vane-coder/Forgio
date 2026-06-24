package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "refresh_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "token_id") private UUID tokenId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false, unique = true, length = 1000) private String token;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    private boolean revoked = false;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}
