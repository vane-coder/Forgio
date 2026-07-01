package com.forgio.controller;

import com.forgio.dto.request.BreakdownLogRequest;
import com.forgio.dto.response.BreakdownLogResponse;
import com.forgio.service.BreakdownLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/breakdown-logs")
public class BreakdownLogController {

    private final BreakdownLogService breakdownLogService;

    public BreakdownLogController(BreakdownLogService breakdownLogService) {
        this.breakdownLogService = breakdownLogService;
    }

    @GetMapping
    public ResponseEntity<List<BreakdownLogResponse>> getAll() {
        return ResponseEntity.ok(breakdownLogService.getAllLogs());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','DEPT_HEAD')")
    public ResponseEntity<BreakdownLogResponse> create(@Valid @RequestBody BreakdownLogRequest request) {
        return ResponseEntity.ok(breakdownLogService.createLog(request));
    }
}
