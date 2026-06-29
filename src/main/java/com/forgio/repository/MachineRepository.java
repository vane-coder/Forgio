package com.forgio.repository;

import com.forgio.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MachineRepository extends JpaRepository<Machine, UUID> {
    List<Machine> findByFactory_FactoryId(UUID factoryId);
    Optional<Machine> findByMachineIdAndFactory_FactoryId(UUID machineId, UUID factoryId);
}