package com.forgio.service;

import com.forgio.entity.Machine;
import com.forgio.entity.ProductionEntry;
import com.forgio.entity.RawMaterial;
import com.forgio.enums.MachineStatus;
import com.forgio.repository.MachineRepository;
import com.forgio.repository.ProductionEntryRepository;
import com.forgio.repository.RawMaterialRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Gathers a short summary of the factory's recent data. Used to build the
 * Claude prompt, and also drives the rules-based fallback.
 */
@Service
@RequiredArgsConstructor
public class FactoryDataSummaryService {

    private final ProductionEntryRepository productionRepository;
    private final RawMaterialRepository materialRepository;
    private final MachineRepository machineRepository;

    public record Summary(
            int productionEntriesLast7Days,
            BigDecimal totalProducedLast7Days,
            List<String> lowStockMaterials,
            int machinesStopped,
            int machinesInMaintenance,
            int totalMachines,
            String text
    ) {}

    @Transactional(readOnly = true)
    public Summary build() {
        UUID factoryId = TenantContext.getFactoryId();
        LocalDate weekAgo = LocalDate.now().minusDays(7);

        // production in the last 7 days
        List<ProductionEntry> recent =
                productionRepository.findByFactory_FactoryIdAndEntryDateBetween(
                        factoryId, weekAgo, LocalDate.now());
        BigDecimal totalProduced = recent.stream()
                .map(ProductionEntry::getQuantityProduced)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // low-stock materials
        List<RawMaterial> materials = materialRepository.findByFactory_FactoryId(factoryId);
        List<String> lowStock = materials.stream()
                .filter(m -> m.getQuantityInStock().compareTo(m.getReorderLevel()) <= 0)
                .map(RawMaterial::getName)
                .toList();

        // machine status
        List<Machine> machines = machineRepository.findByFactory_FactoryId(factoryId);
        int stopped = (int) machines.stream().filter(m -> m.getStatus() == MachineStatus.STOPPED).count();
        int maintenance = (int) machines.stream().filter(m -> m.getStatus() == MachineStatus.MAINTENANCE).count();

        String text = String.format(
                "In the last 7 days the factory recorded %d production entries totalling %s units. " +
                "Low-stock materials: %s. " +
                "Machines: %d total, %d stopped, %d under maintenance.",
                recent.size(),
                totalProduced.toPlainString(),
                lowStock.isEmpty() ? "none" : String.join(", ", lowStock),
                machines.size(), stopped, maintenance);

        return new Summary(recent.size(), totalProduced, lowStock,
                stopped, maintenance, machines.size(), text);
    }
}