package com.forgio.controller;

import com.forgio.dto.request.MarketListingRequest;
import com.forgio.dto.response.MarketListingResponse;
import com.forgio.service.MarketplaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    // Browse all active listings across factories
    @GetMapping("/listings")
    public ResponseEntity<List<MarketListingResponse>> browse() {
        return ResponseEntity.ok(marketplaceService.browse());
    }

    // My factory's own listings
    @GetMapping("/listings/mine")
    public ResponseEntity<List<MarketListingResponse>> myListings() {
        return ResponseEntity.ok(marketplaceService.myListings());
    }

    // Create a listing (managers only)
    @PostMapping("/listings")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MarketListingResponse> create(@Valid @RequestBody MarketListingRequest req) {
        return ResponseEntity.ok(marketplaceService.createListing(req));
    }

    // Cancel one of my listings (managers only)
    @PostMapping("/listings/{id}/cancel")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MarketListingResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.cancelListing(id));
    }
}
