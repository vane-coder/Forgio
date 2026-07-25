package com.forgio.service;

import com.forgio.dto.response.MachineResponse;
import com.forgio.entity.Machine;
import com.forgio.repository.MachineRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forgio.enums.MachineStatus;
import com.forgio.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;

    @Transactional
    public MachineResponse updateStatus(UUID machineId, MachineStatus status) {
        UUID factoryId = TenantContext.getFactoryId();
        Machine machine = machineRepository.findByMachineIdAndFactory_FactoryId(machineId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));
        machine.setStatus(status);
        return toResponse(machineRepository.save(machine));
    }

    /** Every query scoped to TenantContext.getFactoryId() — never client input. */
    @Transactional(readOnly = true)
    public List<MachineResponse> listMachines() {
        UUID factoryId = TenantContext.getFactoryId();
        return machineRepository.findByFactory_FactoryId(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    private MachineResponse toResponse(Machine m) {
        return new MachineResponse(
                m.getMachineId(),
                m.getName(),
                m.getStatus(),
                m.getLastServiceDate());
    }
}