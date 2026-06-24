package com.forgio.repository;

import com.forgio.entity.ProductionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionEntryRepository extends JpaRepository<ProductionEntry, UUID> {
    List<ProductionEntry> findByFactory_FactoryId(UUID factoryId);
    Optional<ProductionEntry> findByEntryIdAndFactory_FactoryId(UUID entryId, UUID factoryId);
    List<ProductionEntry> findByFactory_FactoryIdAndEntryDateBetween(UUID factoryId, LocalDate from, LocalDate to);
    List<ProductionEntry> findByWorker_UserId(UUID workerId);

    // Find unlocked entries older than the cutoff so the scheduler can lock them.
    @Query("SELECT p FROM ProductionEntry p WHERE p.locked = false AND p.createdAt < :cutoff")
    List<ProductionEntry> findUnlockedBefore(@Param("cutoff") Instant cutoff);
}
