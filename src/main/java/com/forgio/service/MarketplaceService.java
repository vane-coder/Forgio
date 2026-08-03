package com.forgio.service;

import com.forgio.dto.request.MarketListingRequest;
import com.forgio.dto.request.PurchaseListingRequest;
import com.forgio.dto.response.MarketListingResponse;
import com.forgio.dto.response.PurchaseInitResponse;
import com.forgio.entity.Branch;
import com.forgio.entity.Factory;
import com.forgio.entity.MarketListing;
import com.forgio.entity.MarketTransaction;
import com.forgio.entity.RawMaterial;
import com.forgio.entity.Shipment;
import com.forgio.entity.ShipmentItem;
import com.forgio.entity.User;
import com.forgio.enums.ListingStatus;
import com.forgio.enums.ShipmentStatus;
import com.forgio.enums.TransactionStatus;
import com.forgio.exception.BadRequestException;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.BranchRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.MarketListingRepository;
import com.forgio.repository.MarketTransactionRepository;
import com.forgio.repository.RawMaterialRepository;
import com.forgio.repository.ShipmentRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Marketplace logic. NOTE the multi-tenancy twist:
 *  - BROWSING shows ACTIVE listings from ALL factories (that's the whole point
 *    of a marketplace — you buy from others). So the browse query is NOT scoped
 *    to one factory.
 *  - CREATING/EDITING/BUYING is scoped to your factory and is MANAGER-only —
 *    enforced here AND at the controller with @PreAuthorize, belt and braces.
 */
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final MarketListingRepository listingRepository;
    private final RawMaterialRepository materialRepository;
    private final FactoryRepository factoryRepository;
    private final MarketTransactionRepository transactionRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaystackService paystackService;

    /** Browse all active listings across every factory. Anyone signed in can view. */
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

    /** Create a listing to sell one of MY materials. Manager-only (enforced at controller). */
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

    /** Cancel one of MY listings. Manager-only. */
    @Transactional
    public MarketListingResponse cancelListing(UUID listingId) {
        UUID factoryId = TenantContext.getFactoryId();
        MarketListing listing = listingRepository
                .findByListingIdAndFactory_FactoryId(listingId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found in this factory"));

        listing.setStatus(ListingStatus.CANCELLED);
        return toResponse(listingRepository.save(listing));
    }

    /** Step 1: buyer (manager) taps "Buy" — reserve the purchase and kick off Paystack checkout. */
    @Transactional
    public PurchaseInitResponse purchase(UUID listingId, PurchaseListingRequest req) {
        MarketListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BadRequestException("This listing is no longer available");
        }
        if (req.quantity().compareTo(listing.getQuantity()) > 0) {
            throw new BadRequestException("Only " + listing.getQuantity() + " " + listing.getMaterial().getUnit() + " available");
        }

        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User buyer = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID buyerFactoryId = TenantContext.getFactoryId();
        Factory buyerFactory = factoryRepository.findById(buyerFactoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        // you can't buy your own listing
        if (listing.getFactory().getFactoryId().equals(buyerFactoryId)) {
            throw new BadRequestException("You cannot buy your own listing");
        }

        // destination branch must belong to the buyer's own company
        if (buyerFactory.getCompany() == null) {
            throw new BadRequestException("Set up a branch for your factory before buying from the marketplace");
        }
        Branch destination = branchRepository
                .findByBranchIdAndCompany_CompanyId(req.destinationBranchId(), buyerFactory.getCompany().getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination branch not found in your company"));

        BigDecimal amount = listing.getPricePerUnit().multiply(req.quantity());
        String reference = "FORGIO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        MarketTransaction tx = MarketTransaction.builder()
                .listing(listing)
                .buyerFactory(buyerFactory)
                .buyerUser(buyer)
                .destinationBranch(destination)
                .quantity(req.quantity())
                .amount(amount)
                .paystackReference(reference)
                .status(TransactionStatus.PENDING)
                .build();
        transactionRepository.save(tx);

        PaystackService.PaystackInitResult init = paystackService.initialize(
                buyer.getPhone() + "@forgio.app", // Paystack requires an email; you don't collect one at signup
                amount,
                reference
        );

        return new PurchaseInitResponse(init.authorizationUrl(), init.reference());
    }

    /** Step 2: called from the public callback endpoint after Paystack redirects back. */
    @Transactional
    public boolean verifyAndFulfill(String reference) {
        MarketTransaction tx = transactionRepository.findByPaystackReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (tx.getStatus() == TransactionStatus.COMPLETED) return true;   // already processed, don't double-fulfil
        if (tx.getStatus() == TransactionStatus.FAILED) return false;

        boolean verified = paystackService.verify(reference);
        if (!verified) {
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
            return false;
        }

        MarketListing listing = tx.getListing();
        BigDecimal remaining = listing.getQuantity().subtract(tx.getQuantity());
        listing.setQuantity(remaining.max(BigDecimal.ZERO));
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) listing.setStatus(ListingStatus.SOLD);
        listingRepository.save(listing);

        // stock lands on the buyer's factory (this app tracks raw-material stock per factory, not per branch)
        RawMaterial existing = materialRepository
                .findByFactory_FactoryId(tx.getBuyerFactory().getFactoryId()).stream()
                .filter(m -> m.getName().equalsIgnoreCase(listing.getMaterial().getName()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantityInStock(existing.getQuantityInStock().add(tx.getQuantity()));
            materialRepository.save(existing);
        } else {
            existing = materialRepository.save(RawMaterial.builder()
                    .factory(tx.getBuyerFactory())
                    .name(listing.getMaterial().getName())
                    .unit(listing.getMaterial().getUnit())
                    .quantityInStock(tx.getQuantity())
                    .reorderLevel(BigDecimal.ZERO)
                    .costPerUnit(listing.getPricePerUnit())
                    .build());
        }

        // log it as an already-arrived shipment so it shows up in the Shipments screen
        Shipment shipment = Shipment.builder()
                .fromBranch(tx.getDestinationBranch())
                .toBranch(tx.getDestinationBranch())
                .company(tx.getDestinationBranch().getCompany())
                .status(ShipmentStatus.ARRIVED)
                .notes("Marketplace purchase — " + listing.getMaterial().getName() + " from " + listing.getFactory().getName())
                .arrivedAt(Instant.now())
                .build();
        shipment.getItems().add(ShipmentItem.builder()
                .shipment(shipment)
                .material(existing)
                .description(listing.getMaterial().getName())
                .unit(listing.getMaterial().getUnit())
                .quantity(tx.getQuantity())
                .build());
        shipmentRepository.save(shipment);

        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);
        return true;
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