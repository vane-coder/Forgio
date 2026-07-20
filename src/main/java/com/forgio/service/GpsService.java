package com.forgio.service;

import com.forgio.dto.request.GpsRequest;
import com.forgio.dto.response.GpsResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.GpsLog;
import com.forgio.entity.Shipment;
import com.forgio.entity.User;
import com.forgio.enums.ShipmentStatus;
import com.forgio.exception.BadRequestException;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.GpsLogRepository;
import com.forgio.repository.ShipmentRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * GPS tracking. A driver posts points for a shipment assigned to them while it's
 * in motion; a manager (same company) or the assigned driver can read the track.
 */
@Service
@RequiredArgsConstructor
public class GpsService {

    private final GpsLogRepository gpsLogRepository;
    private final ShipmentRepository shipmentRepository;
    private final FactoryRepository factoryRepository;

    private UUID currentUserId() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUserId();
    }

    private UUID currentCompanyId() {
        Factory factory = factoryRepository.findById(TenantContext.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));
        if (factory.getCompany() == null) {
            throw new BadRequestException("This factory is not part of a multi-branch company");
        }
        return factory.getCompany().getCompanyId();
    }

    /** Driver posts a location point for their own in-transit shipment. */
    @Transactional
    public GpsResponse postPoint(UUID shipmentId, GpsRequest req) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        // must be the assigned driver
        if (shipment.getDriver() == null || !shipment.getDriver().getUserId().equals(currentUserId())) {
            throw new BadRequestException("You are not the driver for this shipment");
        }
        // only while moving
        if (shipment.getStatus() != ShipmentStatus.DEPARTED
                && shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new BadRequestException("GPS can only be posted while the shipment is in motion");
        }

        GpsLog log = GpsLog.builder()
                .shipment(shipment)
                .latitude(req.latitude())
                .longitude(req.longitude())
                .build();

        return toResponse(gpsLogRepository.save(log));
    }

    /** Manager (same company) or the assigned driver reads the track. */
    @Transactional(readOnly = true)
    public List<GpsResponse> getTrack(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        boolean isAssignedDriver = shipment.getDriver() != null
                && shipment.getDriver().getUserId().equals(currentUserId());
        boolean isSameCompany = shipment.getCompany() != null
                && shipment.getCompany().getCompanyId().equals(currentCompanyId());

        if (!isAssignedDriver && !isSameCompany) {
            throw new BadRequestException("You cannot view this shipment's track");
        }

        return gpsLogRepository.findByShipment_ShipmentIdOrderByRecordedAtAsc(shipmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    private GpsResponse toResponse(GpsLog g) {
        return new GpsResponse(g.getLogId(), g.getLatitude(), g.getLongitude(), g.getRecordedAt());
    }
}