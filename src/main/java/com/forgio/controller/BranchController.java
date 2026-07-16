package com.forgio.controller;

import com.forgio.dto.request.BranchRequest;
import com.forgio.dto.response.BranchResponse;
import com.forgio.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    // List my company's branches
    @GetMapping
    public ResponseEntity<List<BranchResponse>> list() {
        return ResponseEntity.ok(branchService.listBranches());
    }

    // Create a new branch (managers only)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody BranchRequest req) {
        return ResponseEntity.ok(branchService.createBranch(req));
    }
}
