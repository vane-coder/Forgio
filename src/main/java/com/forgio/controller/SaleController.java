package com.forgio.controller;

import com.forgio.dto.request.SaleRequest;
import com.forgio.dto.response.SaleResponse;
import com.forgio.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    /** Workers (and above) record a sale of goods. */
    @PostMapping
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest req) {
        return ResponseEntity.ok(saleService.create(req));
    }

    /** Factory-wide sales list — managers and department heads. */
    @GetMapping("/factory")
    @PreAuthorize("hasAnyRole('MANAGER','DEPT_HEAD')")
    public ResponseEntity<List<SaleResponse>> factorySales() {
        return ResponseEntity.ok(saleService.listForFactory());
    }

    /** A worker's own recorded sales. */
    @GetMapping("/me")
    public ResponseEntity<List<SaleResponse>> mySales() {
        return ResponseEntity.ok(saleService.mySales());
    }
}
