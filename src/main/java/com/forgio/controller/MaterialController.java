package com.forgio.controller;

import com.forgio.dto.request.MaterialRequest;
import com.forgio.dto.response.MaterialResponse;
import com.forgio.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    /** Any authenticated user in the factory can view stock. */
    @GetMapping
    public ResponseEntity<List<MaterialResponse>> list() {
        return ResponseEntity.ok(materialService.listMaterials());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','DEPT_HEAD')")
    public ResponseEntity<MaterialResponse> add(@Valid @RequestBody MaterialRequest req) {
        return ResponseEntity.ok(materialService.addMaterial(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','DEPT_HEAD')")
    public ResponseEntity<MaterialResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody MaterialRequest req) {
        return ResponseEntity.ok(materialService.updateMaterial(id, req));
    }
}
