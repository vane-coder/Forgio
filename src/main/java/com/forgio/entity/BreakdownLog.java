package com.forgio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "breakdown_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BreakdownLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "log_id") private UUID logId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "machine_id")   private Machine machine;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "factory_id")   private Factory factory;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reported_by")  private User reportedBy;
    private String cause;
    @Column(name = "start_time") private Instant startTime;
    @Column(name = "end_time")   private Instant endTime;
    private boolean resolved = false;
}
