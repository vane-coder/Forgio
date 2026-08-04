package com.forgio.repository;

import com.forgio.entity.MarketTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MarketTransactionRepository extends JpaRepository<MarketTransaction, UUID> {
    Optional<MarketTransaction> findByPaystackReference(String reference);
}