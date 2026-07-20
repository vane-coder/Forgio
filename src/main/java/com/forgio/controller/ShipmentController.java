package com.forgio.controller;

import com.forgio.dto.request.ShipmentRequest;
import com.forgio.dto.response.ShipmentResponse;
import com.forgio.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> list() {
        return ResponseEntity.ok(shipmentService.listShipments());
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentRequest req) {
        return ResponseEntity.ok(shipmentService.createShipment(req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable java.util.UUID id,
            @RequestBody java.util.Map<String, String> body) {
        com.forgio.enums.ShipmentStatus status =
                com.forgio.enums.ShipmentStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(shipmentService.updateStatus(id, status));
    }
}