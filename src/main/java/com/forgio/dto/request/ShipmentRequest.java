package com.forgio.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShipmentRequest(
        @NotNull(message = "From-branch is required") UUID fromBranchId,
        @NotNull(message = "To-branch is required") UUID toBranchId,
        UUID driverId,      // optional at creation
        String notes,
        List<CargoLine> items
) {
    /** A single cargo line: what material is being shipped, and how much. */
    public record CargoLine(
            @NotNull(message = "Material is required") UUID materialId,
            @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity
    ) {}
}
