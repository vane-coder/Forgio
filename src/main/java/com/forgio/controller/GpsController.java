package com.forgio.controller;

import com.forgio.dto.request.GpsRequest;
import com.forgio.dto.response.GpsResponse;
import com.forgio.service.GpsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class GpsController {

    private final GpsService gpsService;

    @PostMapping("/{id}/gps")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<GpsResponse> post(@PathVariable UUID id, @Valid @RequestBody GpsRequest req) {
        return ResponseEntity.ok(gpsService.postPoint(id, req));
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<List<GpsResponse>> track(@PathVariable UUID id) {
        return ResponseEntity.ok(gpsService.getTrack(id));
    }
}