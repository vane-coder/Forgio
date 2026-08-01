package com.forgio.service;

import com.forgio.dto.request.ReportsRequest;
import com.forgio.dto.response.ReportsResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.Machine;
import com.forgio.entity.ProductionEntry;
import com.forgio.entity.RawMaterial;
import com.forgio.entity.Report;
import com.forgio.entity.User;
import com.forgio.enums.MachineStatus;
import com.forgio.repository.MachineRepository;
import com.forgio.repository.ProductionEntryRepository;
import com.forgio.repository.RawMaterialRepository;
import com.forgio.repository.ReportsRepository;
import com.forgio.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ReportsService {
    private final ReportsRepository reportsRepository;
    private final ProductionEntryRepository productionRepository;
    private final RawMaterialRepository materialRepository;
    private final MachineRepository machineRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ReportsService(ReportsRepository reportsRepository,
                          ProductionEntryRepository productionRepository,
                          RawMaterialRepository materialRepository,
                          MachineRepository machineRepository) {
        this.reportsRepository = reportsRepository;
        this.productionRepository = productionRepository;
        this.materialRepository = materialRepository;
        this.machineRepository = machineRepository;
    }

    @Transactional(readOnly = true)
    public List<ReportsResponse> getAllReports() {
        UUID factoryId = TenantContext.getFactoryId();

        return reportsRepository.findByFactory_FactoryId(factoryId).stream()
                .map(report -> new ReportsResponse(
                        report.getReportId(),
                        report.getTitle(),
                        "COMPLETED",
                        report.getPeriodStart(),
                        report.getPeriodEnd(),
                        0,
                        null,
                        0,
                        0,
                        report.getContent()
                )).toList();
    }

    @Transactional
    public ReportsResponse generateReport(ReportsRequest request) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factoryReference = entityManager.getReference(Factory.class, factoryId);

        // the logged-in user is who generated the report
        User creator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // ── Compute real metrics for the requested period ───────────
        List<ProductionEntry> entries = productionRepository
                .findByFactory_FactoryIdAndEntryDateBetween(factoryId, request.startDate(), request.endDate());
        BigDecimal totalProduced = entries.stream()
                .map(ProductionEntry::getQuantityProduced)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RawMaterial> materials = materialRepository.findByFactory_FactoryId(factoryId);
        List<String> lowStock = materials.stream()
                .filter(m -> m.getQuantityInStock().compareTo(m.getReorderLevel()) <= 0)
                .map(RawMaterial::getName)
                .toList();

        List<Machine> machines = machineRepository.findByFactory_FactoryId(factoryId);
        int stopped = (int) machines.stream().filter(m -> m.getStatus() == MachineStatus.STOPPED).count();
        int maintenance = (int) machines.stream().filter(m -> m.getStatus() == MachineStatus.MAINTENANCE).count();

        String title = (request.title() != null && !request.title().isBlank())
                ? request.title().trim()
                : "Production report " + request.startDate() + " to " + request.endDate();

        String content = String.format(
                "Report period: %s to %s.%n" +
                "Production entries: %d, totalling %s units produced.%n" +
                "Low-stock materials (%d): %s.%n" +
                "Machines: %d total, %d stopped, %d under maintenance.",
                request.startDate(), request.endDate(),
                entries.size(), totalProduced.toPlainString(),
                lowStock.size(), lowStock.isEmpty() ? "none" : String.join(", ", lowStock),
                machines.size(), stopped, maintenance);

        Report report = Report.builder()
                .factory(factoryReference)
                .title(title)
                .periodStart(request.startDate())
                .periodEnd(request.endDate())
                .generatedBy(creator)
                .content(content)
                .build();

        Report saved = reportsRepository.save(report);

        return new ReportsResponse(
                saved.getReportId(),
                saved.getTitle(),
                "COMPLETED",
                saved.getPeriodStart(),
                saved.getPeriodEnd(),
                entries.size(),
                totalProduced.toPlainString(),
                lowStock.size(),
                stopped,
                saved.getContent()
        );
    }
}
