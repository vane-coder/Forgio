package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
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
    
    @Column(name = "report_type")
    private String reportType; // WEEKLY or MONTHLY

    @Column(name = "generated_at")
    private Instant generatedAt;

    private String contentSummary;
}
