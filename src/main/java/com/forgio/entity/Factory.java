package com.forgio.entity;

import com.forgio.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "factories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "factory_id")
    private UUID factoryId;

    @Column(nullable = false)
    private String name;

    private String location;
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan = SubscriptionPlan.BASIC;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
