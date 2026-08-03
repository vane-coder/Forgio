package com.forgio.repository;

import com.forgio.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, UUID> {
    List<RawMaterial> findByFactory_FactoryId(UUID factoryId);
    Optional<RawMaterial> findByMaterialIdAndFactory_FactoryId(UUID materialId, UUID factoryId);

    /** A worker's view: materials in their department plus factory-wide (unassigned) ones. */
    @Query("SELECT m FROM RawMaterial m WHERE m.factory.factoryId = :factoryId " +
           "AND (m.department IS NULL OR m.department.deptId = :deptId)")
    List<RawMaterial> findVisibleToDepartment(@Param("factoryId") UUID factoryId,
                                              @Param("deptId") UUID deptId);
}
