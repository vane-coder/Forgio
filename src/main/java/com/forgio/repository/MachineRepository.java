package com.forgio.repository;

import com.forgio.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MachineRepository extends JpaRepository<Machine, UUID> {
    List<Machine> findByFactory_FactoryId(UUID factoryId);
    Optional<Machine> findByMachineIdAndFactory_FactoryId(UUID machineId, UUID factoryId);

    /** A worker's view: machines in their department plus factory-wide (unassigned) ones. */
    @Query("SELECT m FROM Machine m WHERE m.factory.factoryId = :factoryId " +
           "AND (m.department IS NULL OR m.department.deptId = :deptId)")
    List<Machine> findVisibleToDepartment(@Param("factoryId") UUID factoryId,
                                          @Param("deptId") UUID deptId);
}