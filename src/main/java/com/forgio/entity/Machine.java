package com.forgio.entity;

import com.forgio.enums.MachineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "machines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Machine {
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "machine_id") private UUID machineId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "factory_id") private Factory factory;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MachineStatus status = MachineStatus.RUNNING;
    @Column(name = "last_service_date") private LocalDate lastServiceDate;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private Instant createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private Instant updatedAt;
}
