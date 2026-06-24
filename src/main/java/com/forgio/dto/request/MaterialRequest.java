package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record MaterialRequest(
        @NotBlank String name,
        @NotBlank String unit,
        @NotNull @PositiveOrZero BigDecimal quantityInStock,
        @NotNull @PositiveOrZero BigDecimal reorderLevel,
        @NotNull @PositiveOrZero BigDecimal costPerUnit
) {}
