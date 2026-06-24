package com.forgio.repository;

import com.forgio.entity.MaterialUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaterialUsageRepository extends JpaRepository<MaterialUsage, UUID> {
    List<MaterialUsage> findByEntry_EntryId(UUID entryId);
    List<MaterialUsage> findByFactory_FactoryId(UUID factoryId);
}
