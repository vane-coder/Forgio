package com.forgio.service;

import com.forgio.dto.request.MaterialRequest;
import com.forgio.dto.response.MaterialResponse;
import com.forgio.entity.Department;
import com.forgio.entity.Factory;
import com.forgio.entity.RawMaterial;
import com.forgio.entity.User;
import com.forgio.enums.UserRole;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.DepartmentRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.RawMaterialRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final RawMaterialRepository materialRepository;
    private final FactoryRepository factoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /** Every query below is scoped to TenantContext.getFactoryId() — never client input. */

    @Transactional(readOnly = true)
    public List<MaterialResponse> listMaterials() {
        UUID factoryId = TenantContext.getFactoryId();

        User me = currentUser();
        // Managers/dept-heads see the whole factory; workers see their own
        // department plus factory-wide (unassigned) materials only.
        List<RawMaterial> materials;
        if (me.getRole() == UserRole.MANAGER || me.getRole() == UserRole.SYSTEM_ADMIN
                || me.getRole() == UserRole.DEPT_HEAD) {
            materials = materialRepository.findByFactory_FactoryId(factoryId);
        } else {
            UUID deptId = me.getDepartment() != null ? me.getDepartment().getDeptId() : null;
            materials = materialRepository.findVisibleToDepartment(factoryId, deptId);
        }
        return materials.stream().map(this::toResponse).toList();
    }

    @Transactional
    public MaterialResponse addMaterial(MaterialRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Department department = resolveDepartment(req.departmentId(), factoryId);

        RawMaterial material = materialRepository.save(RawMaterial.builder()
                .factory(factory)
                .department(department)
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
        material.setDepartment(resolveDepartment(req.departmentId(), factoryId));

        return toResponse(materialRepository.save(material));
    }

    /** Worker-facing: deduct consumed stock from a material, clamped at zero. */
    @Transactional
    public MaterialResponse consume(UUID materialId, BigDecimal quantity) {
        UUID factoryId = TenantContext.getFactoryId();
        RawMaterial material = materialRepository
                .findByMaterialIdAndFactory_FactoryId(materialId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found in this factory"));

        BigDecimal newStock = material.getQuantityInStock().subtract(quantity);
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            newStock = BigDecimal.ZERO;
        }
        material.setQuantityInStock(newStock);

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

    private User currentUser() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Department resolveDepartment(UUID departmentId, UUID factoryId) {
        if (departmentId == null) return null;
        return departmentRepository
                .findByDeptIdAndFactory_FactoryId(departmentId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found in this factory"));
    }

    private MaterialResponse toResponse(RawMaterial m) {
        boolean lowStock = m.getQuantityInStock().compareTo(m.getReorderLevel()) <= 0;
        Department d = m.getDepartment();
        return new MaterialResponse(
                m.getMaterialId(), m.getName(), m.getUnit(),
                m.getQuantityInStock(), m.getReorderLevel(), m.getCostPerUnit(),
                lowStock,
                d != null ? d.getDeptId() : null,
                d != null ? d.getName()   : null);
    }
}
