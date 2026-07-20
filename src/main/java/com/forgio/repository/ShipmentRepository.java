package com.forgio.repository;

import com.forgio.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    // all shipments of my company, newest first
    List<Shipment> findByCompany_CompanyIdOrderByCreatedAtDesc(UUID companyId);

    // a specific shipment scoped to my company
    Optional<Shipment> findByShipmentIdAndCompany_CompanyId(UUID shipmentId, UUID companyId);

    // shipments assigned to a specific driver
    List<Shipment> findByDriver_UserIdOrderByCreatedAtDesc(UUID driverId);
}