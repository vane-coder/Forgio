package com.forgio.service;

import com.forgio.dto.request.SaleRequest;
import com.forgio.dto.response.SaleResponse;
import com.forgio.entity.Department;
import com.forgio.entity.Factory;
import com.forgio.entity.Notification;
import com.forgio.entity.RawMaterial;
import com.forgio.entity.Sale;
import com.forgio.entity.User;
import com.forgio.enums.NotificationType;
import com.forgio.enums.UserRole;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.DepartmentRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.NotificationRepository;
import com.forgio.repository.RawMaterialRepository;
import com.forgio.repository.SaleRepository;
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
public class SaleService {

    private final SaleRepository saleRepository;
    private final FactoryRepository factoryRepository;
    private final RawMaterialRepository materialRepository;
    private final NotificationRepository notificationRepository;
    private final DepartmentRepository departmentRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    public SaleResponse create(SaleRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));
        User seller = currentUser();

        RawMaterial material = null;
        if (req.materialId() != null) {
            material = materialRepository
                    .findByMaterialIdAndFactory_FactoryId(req.materialId(), factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Material not found in this factory"));
        }

        // A dept head selling stock defaults the sale to their own department if none was given.
        Department department = null;
        UUID effectiveDeptId = req.departmentId();
        if (effectiveDeptId == null && seller.getRole() == UserRole.DEPT_HEAD && seller.getDepartment() != null) {
            effectiveDeptId = seller.getDepartment().getDeptId();
        }
        if (effectiveDeptId != null) {
            department = departmentRepository
                    .findByDeptIdAndFactory_FactoryId(effectiveDeptId, factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found in this factory"));
        }

        BigDecimal total = req.unitPrice().multiply(req.quantity());

        Sale sale = saleRepository.save(Sale.builder()
                .factory(factory)
                .soldBy(seller)
                .material(material)
                .department(department)
                .itemName(req.itemName())
                .quantity(req.quantity())
                .unit(req.unit())
                .unitPrice(req.unitPrice())
                .total(total)
                .soldTo(req.soldTo())
                .notes(req.notes())
                .build());

        // Let managers know a sale was recorded (targeted at the manager role).
        notificationRepository.save(Notification.builder()
                .factory(factory)
                .sentBy(seller)
                .targetRole(UserRole.MANAGER)
                .message(seller.getName() + " recorded a sale: " + req.quantity() + " "
                        + (req.unit() != null ? req.unit() + " of " : "x ") + req.itemName()
                        + (req.soldTo() != null && !req.soldTo().isBlank() ? " to " + req.soldTo() : "")
                        + (department != null ? " [" + department.getName() + "]" : "")
                        + " (GHS " + total + ")")
                .type(NotificationType.GENERAL)
                .build());

        return toResponse(sale);
    }

    /**
     * Factory-wide sales list. A MANAGER sees every department's sales.
     * A DEPT_HEAD only sees their own department's sales plus any
     * factory-wide (unassigned) ones — never another department's.
     */
    @Transactional(readOnly = true)
    public List<SaleResponse> listForFactory() {
        UUID factoryId = TenantContext.getFactoryId();
        User me = currentUser();

        List<Sale> sales;
        if (me.getRole() == UserRole.DEPT_HEAD) {
            UUID deptId = me.getDepartment() != null ? me.getDepartment().getDeptId() : null;
            sales = saleRepository.findVisibleToDepartment(factoryId, deptId);
        } else {
            sales = saleRepository.findByFactory_FactoryIdOrderBySoldAtDesc(factoryId);
        }
        return sales.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> mySales() {
        return saleRepository.findBySoldBy_UserIdOrderBySoldAtDesc(currentUser().getUserId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private SaleResponse toResponse(Sale s) {
        User seller = s.getSoldBy();
        RawMaterial m = s.getMaterial();
        Department d = s.getDepartment();
        return new SaleResponse(
                s.getSaleId(),
                m != null ? m.getMaterialId() : null,
                d != null ? d.getDeptId() : null,
                d != null ? d.getName() : null,
                s.getItemName(),
                s.getQuantity(),
                s.getUnit(),
                s.getUnitPrice(),
                s.getTotal(),
                s.getSoldTo(),
                s.getNotes(),
                seller != null ? seller.getUserId() : null,
                seller != null ? seller.getName() : null,
                s.getSoldAt());
    }
}