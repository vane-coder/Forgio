package com.forgio.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseListingRequest(
        @NotNull @Positive BigDecimal quantity,
        @NotNull(message = "Destination branch is required") UUID destinationBranchId
) {}