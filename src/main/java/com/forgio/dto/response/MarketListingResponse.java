package com.forgio.dto.response;

import com.forgio.enums.ListingStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record MarketListingResponse(
        UUID listingId,
        String materialName,
        String sellerFactoryName,
        String sellerLocation,
        BigDecimal quantity,
        BigDecimal pricePerUnit,
        String category,
        ListingStatus status,
        String description
) {}