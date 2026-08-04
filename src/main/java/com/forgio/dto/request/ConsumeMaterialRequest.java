package com.forgio.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Worker records consumption of a material (deducts stock). */
public record ConsumeMaterialRequest(
        @NotNull @Positive BigDecimal quantity
) {}
