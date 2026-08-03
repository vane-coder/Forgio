package com.forgio.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleResponse(
        UUID saleId,
        UUID materialId,
        String itemName,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal total,
        String soldTo,
        String notes,
        UUID soldById,
        String soldByName,
        Instant soldAt
) {}
