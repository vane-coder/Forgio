package com.forgio.controller;

import com.forgio.dto.request.MarketListingRequest;
import com.forgio.dto.request.PurchaseListingRequest;
import com.forgio.dto.response.MarketListingResponse;
import com.forgio.dto.response.PurchaseInitResponse;
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

    // Browse all active listings across factories — everyone (manager, dept head, driver) can view
    @GetMapping("/listings")
    public ResponseEntity<List<MarketListingResponse>> browse() {
        return ResponseEntity.ok(marketplaceService.browse());
    }

    // My factory's own listings — manager only, this is seller-side bookkeeping
    @GetMapping("/listings/mine")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<MarketListingResponse>> myListings() {
        return ResponseEntity.ok(marketplaceService.myListings());
    }

    // Create a listing — manager only
    @PostMapping("/listings")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MarketListingResponse> create(@Valid @RequestBody MarketListingRequest req) {
        return ResponseEntity.ok(marketplaceService.createListing(req));
    }

    // Cancel one of my listings — manager only
    @PostMapping("/listings/{id}/cancel")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<MarketListingResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(marketplaceService.cancelListing(id));
    }

    // Buy — manager only
    @PostMapping("/listings/{id}/purchase")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PurchaseInitResponse> purchase(@PathVariable UUID id,
                                                          @Valid @RequestBody PurchaseListingRequest req) {
        return ResponseEntity.ok(marketplaceService.purchase(id, req));
    }

    // PUBLIC — Paystack redirects the browser/webview here after checkout, no JWT available at this point
    @GetMapping("/transactions/callback")
    public ResponseEntity<Void> callback(@RequestParam(required = false) String reference,
                                          @RequestParam(required = false) String trxref) {
        String ref = reference != null ? reference : trxref;
        boolean ok = marketplaceService.verifyAndFulfill(ref);
        String redirect = "forgio://payment-result?status=" + (ok ? "success" : "failed") + "&reference=" + ref;
        return ResponseEntity.status(302).header("Location", redirect).build();
    }
}