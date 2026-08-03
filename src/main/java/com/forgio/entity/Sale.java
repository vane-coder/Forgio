package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sale_id")
    private UUID saleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sold_by", nullable = false)
    private User soldBy;

    /** Optional link to a stocked material; null for a free-typed item. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private RawMaterial material;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "sold_to")
    private String soldTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "sold_at", updatable = false)
    private Instant soldAt;
}
