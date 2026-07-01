package com.forgio.repository;

import com.forgio.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportsRepository extends JpaRepository<Report, UUID> {
    // Tenant finders matching the factory relationship mapping
    List<Report> findByFactory_FactoryId(UUID factoryId);
    Optional<Report> findByReportIdAndFactory_FactoryId(UUID reportId, UUID factoryId);
}
