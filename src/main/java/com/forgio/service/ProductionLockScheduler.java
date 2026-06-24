package com.forgio.service;

import com.forgio.entity.ProductionEntry;
import com.forgio.repository.ProductionEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Enforces the data-integrity rule from the proposal: production entries are
 * locked 24 hours after submission to prevent backdating. Runs hourly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductionLockScheduler {

    private final ProductionEntryRepository productionRepository;

    @Scheduled(cron = "0 0 * * * *")   // top of every hour
    @Transactional
    public void lockOldEntries() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        List<ProductionEntry> toLock = productionRepository.findUnlockedBefore(cutoff);
        toLock.forEach(e -> e.setLocked(true));
        if (!toLock.isEmpty()) {
            productionRepository.saveAll(toLock);
            log.info("Locked {} production entries older than 24h", toLock.size());
        }
    }
}
