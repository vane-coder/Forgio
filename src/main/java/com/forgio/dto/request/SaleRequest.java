package com.forgio.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleRequest(
        UUID materialId,                              // optional link to stocked material
        UUID departmentId,                             // optional: which department this sale belongs to
        @NotBlank String itemName,
        @NotNull @Positive BigDecimal quantity,
        String unit,
        @NotNull @PositiveOrZero BigDecimal unitPrice,
        String soldTo,
        String notes
) {}