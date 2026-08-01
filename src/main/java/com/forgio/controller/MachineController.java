package com.forgio.controller;

import com.forgio.dto.request.MachineRequest;
import com.forgio.dto.response.MachineResponse;
import com.forgio.service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}