package com.forgio.repository;

import com.forgio.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    List<Sale> findByFactory_FactoryIdOrderBySoldAtDesc(UUID factoryId);
    List<Sale> findBySoldBy_UserIdOrderBySoldAtDesc(UUID userId);
}
