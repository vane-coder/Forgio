package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "waste_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "waste_id")    private UUID wasteId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "entry_id")    private ProductionEntry entry;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "material_id") private RawMaterial material;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "factory_id")  private Factory factory;
    @Column(name = "waste_amount", nullable = false, precision = 12, scale = 3) private BigDecimal wasteAmount;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
}
