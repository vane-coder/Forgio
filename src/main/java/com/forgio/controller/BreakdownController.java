package com.forgio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/report-breakdown")
@RequiredArgsConstructor
public class BreakdownController {

    // This endpoint handles saving a new machine breakdown report
    @PostMapping
    public ResponseEntity<String> reportBreakdown() {
        return ResponseEntity.ok("Breakdown report logged successfully!");
    }
}
