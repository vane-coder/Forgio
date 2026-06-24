package com.forgio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProductionResponse(
        UUID entryId,
        String productName,
        BigDecimal quantityProduced,
        String shift,
        LocalDate entryDate,
        boolean locked,
        UUID workerId,
        String workerName,
        BigDecimal totalMaterialUsed,
        BigDecimal totalWaste,
        BigDecimal estimatedMaterialCost,
        List<MaterialUsageResponse> materials
) {
    public record MaterialUsageResponse(
            UUID materialId,
            String materialName,
            BigDecimal quantityUsed,
            BigDecimal wasteAmount
    ) {}
}
