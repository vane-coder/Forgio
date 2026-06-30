package com.forgio.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/report-breakdown")
@RequiredArgsConstructor
public class BreakdownController {

    @PostMapping
    public ResponseEntity<String> reportBreakdown(@Valid @RequestBody com.forgio.dto.BreakdownRequest request) {
        System.out.println("Received breakdown for machine: " + request.getMachineId());
        
        return ResponseEntity.ok("Breakdown report logged successfully!");
    }
}
