package com.forgio.repository;

import com.forgio.entity.GpsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GpsLogRepository extends JpaRepository<GpsLog, UUID> {
    // a shipment's track, oldest point first
    List<GpsLog> findByShipment_ShipmentIdOrderByRecordedAtAsc(UUID shipmentId);
}