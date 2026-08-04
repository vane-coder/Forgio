package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private UUID reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id")
    private Factory factory;

    private String title;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    private String content;

    @Column(name = "production_entries")
    private Integer productionEntries;

    @Column(name = "total_produced", precision = 12, scale = 3)
    private BigDecimal totalProduced;

    @Column(name = "low_stock_materials")
    private Integer lowStockMaterials;

    @Column(name = "machines_stopped")
    private Integer machinesStopped;
}