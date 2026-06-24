package com.forgio.service;

import com.forgio.dto.request.MaterialRequest;
import com.forgio.dto.response.MaterialResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.RawMaterial;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.RawMaterialRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final RawMaterialRepository materialRepository;
    private final FactoryRepository factoryRepository;

    /** Every query below is scoped to TenantContext.getFactoryId() — never client input. */

    @Transactional(readOnly = true)
    public List<MaterialResponse> listMaterials() {
        UUID factoryId = TenantContext.getFactoryId();
        return materialRepository.findByFactory_FactoryId(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MaterialResponse addMaterial(MaterialRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        RawMaterial material = materialRepository.save(RawMaterial.builder()
                .factory(factory)
                .name(req.name())
                .unit(req.unit())
                .quantityInStock(req.quantityInStock())
                .reorderLevel(req.reorderLevel())
                .costPerUnit(req.costPerUnit())
                .build());

        return toResponse(material);
    }

    @Transactional
    public MaterialResponse updateMaterial(UUID materialId, MaterialRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        RawMaterial material = materialRepository
                .findByMaterialIdAndFactory_FactoryId(materialId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found in this factory"));

        material.setName(req.name());
        material.setUnit(req.unit());
        material.setQuantityInStock(req.quantityInStock());
        material.setReorderLevel(req.reorderLevel());
        material.setCostPerUnit(req.costPerUnit());

        return toResponse(materialRepository.save(material));
    }

    /** Internal: used by ProductionService to deduct consumed stock. */
    @Transactional(readOnly = true)
    public RawMaterial getOwnedMaterial(UUID materialId) {
        UUID factoryId = TenantContext.getFactoryId();
        return materialRepository
                .findByMaterialIdAndFactory_FactoryId(materialId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Material " + materialId + " not found in this factory"));
    }

    private MaterialResponse toResponse(RawMaterial m) {
        boolean lowStock = m.getQuantityInStock().compareTo(m.getReorderLevel()) <= 0;
        return new MaterialResponse(
                m.getMaterialId(), m.getName(), m.getUnit(),
                m.getQuantityInStock(), m.getReorderLevel(), m.getCostPerUnit(),
                lowStock);
    }
}
