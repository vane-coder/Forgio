package com.forgio.service;

import com.forgio.dto.request.ProductionRequest;
import com.forgio.dto.response.ProductionResponse;
import com.forgio.entity.*;
import com.forgio.exception.BadRequestException;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.*;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionService {

    private final ProductionEntryRepository productionRepository;
    private final MaterialUsageRepository usageRepository;
    private final WasteRecordRepository wasteRepository;
    private final RawMaterialRepository materialRepository;
    private final FactoryRepository factoryRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Submits a production entry. In one transaction we:
     *   1. save the entry,
     *   2. record material usage + deduct it from stock,
     *   3. record measured waste,
     *   4. flag any material that drops below its reorder level (low-stock alert hook).
     */
    @Transactional
    public ProductionResponse submitProduction(ProductionRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        User worker = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Department department = null;
        if (req.departmentId() != null) {
            department = departmentRepository
                    .findByDeptIdAndFactory_FactoryId(req.departmentId(), factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found in this factory"));
        }

        ProductionEntry entry = productionRepository.save(ProductionEntry.builder()
                .factory(factory)
                .worker(worker)
                .department(department)
                .productName(req.productName())
                .quantityProduced(req.quantityProduced())
                .shift(req.shift())
                .entryDate(LocalDate.now())
                .locked(false)
                .notes(req.notes())
                .build());

        BigDecimal totalUsed = BigDecimal.ZERO;
        BigDecimal totalWaste = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<ProductionResponse.MaterialUsageResponse> lines = new ArrayList<>();

        if (req.materialsUsed() != null) {
            for (var line : req.materialsUsed()) {
                RawMaterial material = materialRepository
                        .findByMaterialIdAndFactory_FactoryId(line.materialId(), factoryId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Material " + line.materialId() + " not found in this factory"));

                if (material.getQuantityInStock().compareTo(line.quantityUsed()) < 0) {
                    throw new BadRequestException(
                            "Not enough stock of " + material.getName()
                            + " (have " + material.getQuantityInStock()
                            + ", need " + line.quantityUsed() + ")");
                }

                // Deduct from stock
                material.setQuantityInStock(material.getQuantityInStock().subtract(line.quantityUsed()));
                materialRepository.save(material);

                usageRepository.save(MaterialUsage.builder()
                        .entry(entry).material(material).factory(factory)
                        .quantityUsed(line.quantityUsed())
                        .build());

                BigDecimal waste = line.wasteAmount() != null ? line.wasteAmount() : BigDecimal.ZERO;
                if (waste.compareTo(BigDecimal.ZERO) > 0) {
                    wasteRepository.save(WasteRecord.builder()
                            .entry(entry).material(material).factory(factory)
                            .wasteAmount(waste)
                            .build());
                }

                totalUsed = totalUsed.add(line.quantityUsed());
                totalWaste = totalWaste.add(waste);
                totalCost = totalCost.add(line.quantityUsed().multiply(material.getCostPerUnit()));

                // Low-stock alert hook — wire to NotificationService/FCM when that slice is added.
                if (material.getQuantityInStock().compareTo(material.getReorderLevel()) <= 0) {
                    log.warn("LOW STOCK [factory={}] {} at {} (reorder level {})",
                            factoryId, material.getName(),
                            material.getQuantityInStock(), material.getReorderLevel());
                }

                lines.add(new ProductionResponse.MaterialUsageResponse(
                        material.getMaterialId(), material.getName(),
                        line.quantityUsed(), waste));
            }
        }

        return new ProductionResponse(
                entry.getEntryId(), entry.getProductName(), entry.getQuantityProduced(),
                entry.getShift(), entry.getEntryDate(), entry.isLocked(),
                worker.getUserId(), worker.getName(),
                totalUsed, totalWaste, totalCost, lines);
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> listFactoryProduction() {
        UUID factoryId = TenantContext.getFactoryId();
        return productionRepository.findByFactory_FactoryId(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> myProduction() {
        User worker = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return productionRepository.findByWorker_UserId(worker.getUserId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductionResponse toResponse(ProductionEntry entry) {
        List<MaterialUsage> usages = usageRepository.findByEntry_EntryId(entry.getEntryId());
        List<WasteRecord> wastes = wasteRepository.findByEntry_EntryId(entry.getEntryId());

        BigDecimal totalUsed = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<ProductionResponse.MaterialUsageResponse> lines = new ArrayList<>();

        for (MaterialUsage u : usages) {
            totalUsed = totalUsed.add(u.getQuantityUsed());
            totalCost = totalCost.add(u.getQuantityUsed().multiply(u.getMaterial().getCostPerUnit()));
            BigDecimal waste = wastes.stream()
                    .filter(w -> w.getMaterial().getMaterialId().equals(u.getMaterial().getMaterialId()))
                    .map(WasteRecord::getWasteAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lines.add(new ProductionResponse.MaterialUsageResponse(
                    u.getMaterial().getMaterialId(), u.getMaterial().getName(),
                    u.getQuantityUsed(), waste));
        }

        BigDecimal totalWaste = wastes.stream()
                .map(WasteRecord::getWasteAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductionResponse(
                entry.getEntryId(), entry.getProductName(), entry.getQuantityProduced(),
                entry.getShift(), entry.getEntryDate(), entry.isLocked(),
                entry.getWorker().getUserId(), entry.getWorker().getName(),
                totalUsed, totalWaste, totalCost, lines);
    }
}
