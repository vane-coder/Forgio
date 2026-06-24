package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "raw_materials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RawMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "material_id")
    private UUID materialId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit;

    @Column(name = "quantity_in_stock", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityInStock = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false, precision = 12, scale = 3)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "cost_per_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private Instant updatedAt;
}
