package com.forgio.repository;

import com.forgio.entity.MarketListing;
import com.forgio.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketListingRepository extends JpaRepository<MarketListing, UUID> {

    // Browse: all ACTIVE listings across ALL factories (the marketplace view)
    List<MarketListing> findByStatus(ListingStatus status);

    // My factory's own listings
    List<MarketListing> findByFactory_FactoryId(UUID factoryId);

    // A specific listing that belongs to my factory (for editing/cancelling)
    Optional<MarketListing> findByListingIdAndFactory_FactoryId(UUID listingId, UUID factoryId);
}