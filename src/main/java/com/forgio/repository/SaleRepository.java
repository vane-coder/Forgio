package com.forgio.repository;

import com.forgio.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    List<Sale> findByFactory_FactoryIdOrderBySoldAtDesc(UUID factoryId);
    List<Sale> findBySoldBy_UserIdOrderBySoldAtDesc(UUID userId);

    /** A dept head's view: sales in their own department plus factory-wide (unassigned) ones. */
    @Query("SELECT s FROM Sale s WHERE s.factory.factoryId = :factoryId " +
           "AND (s.department IS NULL OR s.department.deptId = :deptId) " +
           "ORDER BY s.soldAt DESC")
    List<Sale> findVisibleToDepartment(@Param("factoryId") UUID factoryId,
                                        @Param("deptId") UUID deptId);
}