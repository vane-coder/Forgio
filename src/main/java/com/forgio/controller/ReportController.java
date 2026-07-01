package com.forgio.controller;

import com.forgio.dto.request.ReportsRequest;
import com.forgio.dto.response.ReportsResponse;
import com.forgio.service.ReportsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportsService reportsService;

    public ReportController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping
    public ResponseEntity<List<ReportsResponse>> getAll() {
        return ResponseEntity.ok(reportsService.getAllReports());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','DEPT_HEAD')")
    public ResponseEntity<ReportsResponse> create(@Valid @RequestBody ReportsRequest request) {
        return ResponseEntity.ok(reportsService.generateReport(request));
    }
}
