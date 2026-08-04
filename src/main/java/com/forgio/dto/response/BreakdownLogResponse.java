package com.forgio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BreakdownLogResponse {
    private UUID id;
    private String status;
    private String message;
    private UUID machineId;
    private String machineName;
    private String cause;
    private Instant startTime;
    private boolean resolved;
    private String reportedByName;
}
