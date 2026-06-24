package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "production_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "entry_id")
    private UUID entryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity_produced", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityProduced;

    private String shift;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /** Locked after 24 hours – prevents backdating. */
    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
