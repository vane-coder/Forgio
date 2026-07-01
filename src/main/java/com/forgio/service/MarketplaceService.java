package com.forgio.service;

import com.forgio.dto.request.MarketListingRequest;
import com.forgio.dto.response.MarketListingResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.MarketListing;
import com.forgio.entity.RawMaterial;
import com.forgio.enums.ListingStatus;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.MarketListingRepository;
import com.forgio.repository.RawMaterialRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Marketplace logic. NOTE the multi-tenancy twist:
 *  - BROWSING shows ACTIVE listings from ALL factories (that's the whole point
 *    of a marketplace — you buy from others). So the browse query is NOT scoped
 *    to one factory.
 *  - CREATING/EDITING a listing IS scoped to your factory — you can only sell
 *    your own materials and only edit your own listings.
 */
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final MarketListingRepository listingRepository;
    private final RawMaterialRepository materialRepository;
    private final FactoryRepository factoryRepository;

    /** Browse all active listings across every factory. */
    @Transactional(readOnly = true)
    public List<MarketListingResponse> browse() {
        return listingRepository.findByStatus(ListingStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    /** My own factory's listings. */
    @Transactional(readOnly = true)
    public List<MarketListingResponse> myListings() {
        UUID factoryId = TenantContext.getFactoryId();
        return listingRepository.findByFactory_FactoryId(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Create a listing to sell one of MY materials. */
    @Transactional
    public MarketListingResponse createListing(MarketListingRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        // the material being sold must belong to MY factory
        RawMaterial material = materialRepository
                .findByMaterialIdAndFactory_FactoryId(req.materialId(), factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found in this factory"));

        MarketListing listing = MarketListing.builder()
                .factory(factory)
                .material(material)
                .quantity(req.quantity())
                .pricePerUnit(req.pricePerUnit())
                .category(req.category())
                .description(req.description())
                .status(ListingStatus.ACTIVE)
                .build();

        return toResponse(listingRepository.save(listing));
    }

    /** Cancel one of MY listings. */
    @Transactional
    public MarketListingResponse cancelListing(UUID listingId) {
        UUID factoryId = TenantContext.getFactoryId();
        MarketListing listing = listingRepository
                .findByListingIdAndFactory_FactoryId(listingId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found in this factory"));

        listing.setStatus(ListingStatus.CANCELLED);
        return toResponse(listingRepository.save(listing));
    }

    private MarketListingResponse toResponse(MarketListing l) {
        Factory f = l.getFactory();
        return new MarketListingResponse(
                l.getListingId(),
                l.getMaterial().getName(),
                f.getName(),
                f.getLocation(),
                l.getQuantity(),
                l.getPricePerUnit(),
                l.getCategory(),
                l.getStatus(),
                l.getDescription());
    }
}