package com.forgio.dto.response;

import com.forgio.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
        List<CargoItem> items,
        Instant departedAt,
        Instant arrivedAt,
        Instant createdAt
) {
    /** A single cargo line on a shipment. */
    public record CargoItem(
            UUID materialId,
            String materialName,
            BigDecimal quantity,
            String unit
    ) {}
}
