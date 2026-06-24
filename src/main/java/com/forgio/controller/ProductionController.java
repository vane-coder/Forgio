package com.forgio.controller;

import com.forgio.dto.request.ProductionRequest;
import com.forgio.dto.response.ProductionResponse;
import com.forgio.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/production")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    /** Workers (and above) submit their daily output. */
    @PostMapping
    public ResponseEntity<ProductionResponse> submit(@Valid @RequestBody ProductionRequest req) {
        return ResponseEntity.ok(productionService.submitProduction(req));
    }

    /** Factory-wide production list — managers and department heads. */
    @GetMapping("/factory")
    @PreAuthorize("hasAnyRole('MANAGER','DEPT_HEAD')")
    public ResponseEntity<List<ProductionResponse>> factoryProduction() {
        return ResponseEntity.ok(productionService.listFactoryProduction());
    }

    /** A worker's own past entries. */
    @GetMapping("/me")
    public ResponseEntity<List<ProductionResponse>> myProduction() {
        return ResponseEntity.ok(productionService.myProduction());
    }
}
