package com.forgio.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record ReportsResponse(
        UUID reportId,
        String title,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int productionEntries,
        String totalProduced,
        int lowStockMaterials,
        int machinesStopped,
        String content
) {}
