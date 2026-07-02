package com.forgio.service;

import com.forgio.dto.request.ReportsRequest;
import com.forgio.dto.response.ReportsResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.Report;
import com.forgio.repository.ReportsRepository;
import com.forgio.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    private final ReportsRepository reportsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ReportsService(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    public List<ReportsResponse> getAllReports() {
        // 1. Read factory strictly from token context
        UUID factoryId = TenantContext.getFactoryId();
        
        // 2. Scope every database call with that factoryId
        return reportsRepository.findByFactory_FactoryId(factoryId).stream()
                .map(report -> new ReportsResponse(
                        report.getReportId(),
                        "COMPLETED",
                        report.getTitle() + " (" + report.getReportType() + ")"
                )).collect(Collectors.toList());
    }

    @Transactional
    public ReportsResponse generateReport(ReportsRequest request) {
        // 3. Never read factoryId from the request
        UUID factoryId = TenantContext.getFactoryId();

        // 4. Safely query context shells to maintain isolation rules
        Factory factoryReference = entityManager.getReference(Factory.class, factoryId);

        Report report = Report.builder()
                .factory(factoryReference)
                .title(request.getTitle())
                .reportType(request.getReportType().toUpperCase())
                .generatedAt(Instant.now())
                .contentSummary("Automated production snapshot metrics compiled.")
                .build();

        Report saved = reportsRepository.save(report);
        
        return new ReportsResponse(
                saved.getReportId(),
                "CREATED",
                "Report generated successfully"
        );
    }
}
