package com.forgio.service;

import com.forgio.dto.request.ReportsRequest;
import com.forgio.dto.response.ReportsResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.Report;
import com.forgio.entity.User;
import com.forgio.repository.ReportsRepository;
import com.forgio.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        UUID factoryId = TenantContext.getFactoryId();

        return reportsRepository.findByFactory_FactoryId(factoryId).stream()
                .map(report -> new ReportsResponse(
                        report.getReportId(),
                        "COMPLETED",
                        report.getTitle()
                )).collect(Collectors.toList());
    }

    @Transactional
    public ReportsResponse generateReport(ReportsRequest request) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factoryReference = entityManager.getReference(Factory.class, factoryId);

        // the logged-in user is who generated the report
        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Report report = Report.builder()
                .factory(factoryReference)
                .title(request.getTitle())
                .generatedBy(creator)
                .content("Automated production snapshot metrics compiled.")
                .build();

        Report saved = reportsRepository.save(report);

        return new ReportsResponse(
                saved.getReportId(),
                "CREATED",
                "Report generated successfully"
        );
    }
}