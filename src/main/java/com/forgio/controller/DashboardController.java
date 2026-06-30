package com.forgio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    // This path lets the dashboard screen fetch high-level summary statistics
    @GetMapping
    public ResponseEntity<String> getDashboardSummaryData() {
        return ResponseEntity.ok("Dashboard summary data fetched successfully!");
    }
}
