package com.forgio.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record MarketListingRequest(
        @NotNull(message = "Material is required") UUID materialId,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @Positive BigDecimal pricePerUnit,
        String category,
        String description
) {}