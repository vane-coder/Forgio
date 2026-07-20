package com.forgio.service;

import com.forgio.dto.request.ShipmentRequest;
import com.forgio.dto.response.ShipmentResponse;
import com.forgio.entity.Branch;
import com.forgio.entity.Factory;
import com.forgio.entity.Shipment;
import com.forgio.entity.User;
import com.forgio.enums.ShipmentStatus;
import com.forgio.enums.UserRole;
import com.forgio.exception.BadRequestException;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.BranchRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.ShipmentRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Shipments move goods between branches of the SAME company. Everything is
 * COMPANY-scoped (not single-factory): we resolve the current user's factory,
 * then its company, and scope all queries by that company.
 */
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final BranchRepository branchRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;

    /** Resolve the current user's company id from their factory. */
    private UUID currentCompanyId() {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));
        if (factory.getCompany() == null) {
            throw new BadRequestException("This factory is not part of a multi-branch company");
        }
        return factory.getCompany().getCompanyId();
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> listShipments() {
        UUID companyId = currentCompanyId();
        return shipmentRepository.findByCompany_CompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest req) {
        UUID companyId = currentCompanyId();

        // both branches must belong to my company
        Branch from = branchRepository.findByBranchIdAndCompany_CompanyId(req.fromBranchId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("From-branch not found in this company"));
        Branch to = branchRepository.findByBranchIdAndCompany_CompanyId(req.toBranchId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("To-branch not found in this company"));

        // optional driver: must be a DRIVER in my company
        User driver = null;
        if (req.driverId() != null) {
            driver = userRepository.findById(req.driverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
            if (driver.getRole() != UserRole.DRIVER) {
                throw new BadRequestException("Assigned user is not a driver");
            }
        }

        Shipment shipment = Shipment.builder()
                .fromBranch(from)
                .toBranch(to)
                .driver(driver)
                .company(from.getCompany())
                .status(ShipmentStatus.PENDING)
                .notes(req.notes())
                .build();

        return toResponse(shipmentRepository.save(shipment));
    }
   /** Driver advances their own shipment's status, forward-only. */
    @Transactional
    public ShipmentResponse updateStatus(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        // must be the assigned driver
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (shipment.getDriver() == null || !shipment.getDriver().getUserId().equals(principal.getUserId())) {
            throw new BadRequestException("You are not the driver for this shipment");
        }

        // forward-only: PENDING -> DEPARTED -> IN_TRANSIT -> ARRIVED
        int current = shipment.getStatus().ordinal();
        int next = newStatus.ordinal();
        if (next != current + 1) {
            throw new BadRequestException("Invalid status transition from "
                    + shipment.getStatus() + " to " + newStatus);
        }

        shipment.setStatus(newStatus);
        if (newStatus == ShipmentStatus.DEPARTED) shipment.setDepartedAt(java.time.Instant.now());
        if (newStatus == ShipmentStatus.ARRIVED)  shipment.setArrivedAt(java.time.Instant.now());

        return toResponse(shipmentRepository.save(shipment));
    }


    private ShipmentResponse toResponse(Shipment s) {
        Branch fb = s.getFromBranch();
        Branch tb = s.getToBranch();
        User d = s.getDriver();
        return new ShipmentResponse(
                s.getShipmentId(),
                fb != null ? fb.getBranchId() : null,
                fb != null ? fb.getName()     : null,
                tb != null ? tb.getBranchId() : null,
                tb != null ? tb.getName()     : null,
                d != null ? d.getUserId() : null,
                d != null ? d.getName()   : null,
                s.getStatus(),
                s.getNotes(),
                s.getDepartedAt(),
                s.getArrivedAt(),
                s.getCreatedAt());
    }
}