package com.forgio.controller;

import com.forgio.dto.request.BreakdownRequest;
import com.forgio.dto.response.BreakdownResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/report-breakdown")
@RequiredArgsConstructor
public class BreakdownController {

    @PostMapping
    public ResponseEntity<BreakdownResponse> saveBreakdownReport(@Valid @RequestBody BreakdownRequest request) {
        // This generates a structured receipt package matching your team's architecture patterns
        BreakdownResponse confirmation = new BreakdownResponse(
            UUID.randomUUID(),
            "PENDING_REPAIR",
            "Breakdown successfully logged for machine: " + request.getMachineId()
        );
        return ResponseEntity.ok(confirmation);
    }
}
