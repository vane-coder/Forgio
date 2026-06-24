package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "material_usage")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterialUsage {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "usage_id") private UUID usageId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "entry_id")    private ProductionEntry entry;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "material_id") private RawMaterial material;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "factory_id")  private Factory factory;
    @Column(name = "quantity_used", nullable = false, precision = 12, scale = 3) private BigDecimal quantityUsed;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}
