package com.forgio.controller;

import com.forgio.dto.request.MachineRequest;
import com.forgio.dto.request.CreateMachineRequest;
import com.forgio.dto.response.MachineResponse;
import com.forgio.service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.forgio.enums.MachineStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @GetMapping
    public ResponseEntity<List<MachineResponse>> list() {
        return ResponseEntity.ok(machineService.listMachines());
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MachineResponse> add(@Valid @RequestBody MachineRequest req) {
        return ResponseEntity.ok(machineService.createMachine(req));
    }
    public ResponseEntity<MachineResponse> create(@Valid @RequestBody CreateMachineRequest req) {
        return ResponseEntity.ok(machineService.createMachine(req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MachineResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        MachineStatus status = MachineStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(machineService.updateStatus(id, status));
    }
}