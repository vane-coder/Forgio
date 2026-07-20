package com.forgio.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShipmentRequest(
        @NotNull(message = "From-branch is required") UUID fromBranchId,
        @NotNull(message = "To-branch is required") UUID toBranchId,
        UUID driverId,      // optional at creation
        String notes
) {}