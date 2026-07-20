package com.forgio.dto.response;

import com.forgio.enums.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(
        UUID shipmentId,
        UUID fromBranchId,
        String fromBranchName,
        UUID toBranchId,
        String toBranchName,
        UUID driverId,
        String driverName,
        ShipmentStatus status,
        String notes,
        Instant departedAt,
        Instant arrivedAt,
        Instant createdAt
) {}