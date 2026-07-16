package com.forgio.controller;

import com.forgio.dto.response.MachineResponse;
import com.forgio.service.MachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}