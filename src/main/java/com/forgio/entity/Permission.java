package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","factory_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "perm_id")
    private UUID permId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @Column(name = "can_view_reports")
    private boolean canViewReports = false;

    @Column(name = "can_manage_workers")
    private boolean canManageWorkers = false;

    @Column(name = "can_approve_marketplace")
    private boolean canApproveMarketplace = false;

    @Column(name = "can_send_notifications")
    private boolean canSendNotifications = false;

    @Column(name = "can_manage_machines")
    private boolean canManageMachines = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
