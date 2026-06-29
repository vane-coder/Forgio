package com.forgio.controller;

import com.forgio.dto.request.PermissionRequest;
import com.forgio.dto.response.PermissionResponse;
import com.forgio.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PermissionResponse> getForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(permissionService.getForUser(userId));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PermissionResponse> assign(@Valid @RequestBody PermissionRequest req) {
        return ResponseEntity.ok(permissionService.assign(req));
    }
}