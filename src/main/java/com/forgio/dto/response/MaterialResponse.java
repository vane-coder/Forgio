package com.forgio.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record MaterialResponse(
        UUID materialId,
        String name,
        String unit,
        BigDecimal quantityInStock,
        BigDecimal reorderLevel,
        BigDecimal costPerUnit,
        boolean lowStock
) {}
