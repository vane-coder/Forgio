package com.forgio.service;

import com.forgio.dto.response.DashboardResponse;
import com.forgio.repository.BreakdownLogRepository;
import com.forgio.security.TenantContext;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class DashboardService {

    private final BreakdownLogRepository breakdownLogRepository;

    // We inject the Breakdown repository to count logs safely for this specific tenant
    public DashboardService(BreakdownLogRepository breakdownLogRepository) {
        this.breakdownLogRepository = breakdownLogRepository;
    }

    public DashboardResponse getSummaryData() {
        // 1. Read the factory strictly from the secure token
        UUID factoryId = TenantContext.getFactoryId();
        
        // 2. Scope our calculations to only count items belonging to this specific factoryId
        long activeBreakdowns = breakdownLogRepository.findByFactory_FactoryId(factoryId)
                .stream()
                .filter(log -> !log.isResolved())
                .count();

        String status = (activeBreakdowns > 0) ? "ATTENTION REQUIRED" : "ALL SYSTEMS OPERATIONAL";
        String message = "Welcome to the central command dashboard summary view.";

        return new DashboardResponse(
                factoryId,
                status,
                activeBreakdowns,
                message
        );
    }
}
