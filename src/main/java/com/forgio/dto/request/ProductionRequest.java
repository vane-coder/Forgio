package com.forgio.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductionRequest(
        @NotBlank String productName,
        @NotNull @Positive BigDecimal quantityProduced,
        String shift,
        UUID departmentId,
        String notes,
        @Valid List<MaterialUsageLine> materialsUsed
) {
    /** One material consumed by this production run, plus optional measured waste. */
    public record MaterialUsageLine(
            @NotNull UUID materialId,
            @NotNull @PositiveOrZero BigDecimal quantityUsed,
            @PositiveOrZero BigDecimal wasteAmount   // nullable; defaults to 0
    ) {}
}
