package com.forgio.repository;

import com.forgio.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, UUID> {
    List<RawMaterial> findByFactory_FactoryId(UUID factoryId);
    Optional<RawMaterial> findByMaterialIdAndFactory_FactoryId(UUID materialId, UUID factoryId);
}
