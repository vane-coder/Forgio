package com.forgio.service;

import com.forgio.dto.response.DashboardResponse;
import com.forgio.entity.ProductionEntry;
import com.forgio.repository.BreakdownLogRepository;
import com.forgio.repository.ProductionEntryRepository;
import com.forgio.security.TenantContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardService {

    private final BreakdownLogRepository breakdownLogRepository;
    private final ProductionEntryRepository productionRepository;

    public DashboardService(BreakdownLogRepository breakdownLogRepository,
                             ProductionEntryRepository productionRepository) {
        this.breakdownLogRepository = breakdownLogRepository;
        this.productionRepository = productionRepository;
    }

    public DashboardResponse getSummaryData() {
        UUID factoryId = TenantContext.getFactoryId();

        long activeBreakdowns = breakdownLogRepository.findByFactory_FactoryId(factoryId)
                .stream()
                .filter(log -> !log.isResolved())
                .count();

        String status = (activeBreakdowns > 0) ? "ATTENTION REQUIRED" : "ALL SYSTEMS OPERATIONAL";
        String message = "Welcome to the central command dashboard summary view.";

        // group today's production by product + department
        LocalDate today = LocalDate.now();
        Map<String, DashboardResponse.TodayProductionLine> grouped = new LinkedHashMap<>();
        for (ProductionEntry e : productionRepository.findByFactory_FactoryId(factoryId)) {
            if (!e.getEntryDate().equals(today)) continue;
            String deptName = e.getDepartment() != null ? e.getDepartment().getName() : null;
            String key = e.getProductName() + "|" + deptName;
            grouped.merge(key,
                    new DashboardResponse.TodayProductionLine(e.getProductName(), e.getQuantityProduced(), deptName),
                    (a, b) -> new DashboardResponse.TodayProductionLine(
                            a.getProductName(), a.getTotalQuantity().add(b.getTotalQuantity()), a.getDepartmentName()));
        }

        return new DashboardResponse(factoryId, status, activeBreakdowns, message, List.copyOf(grouped.values()));
    }
}