package com.forgio.service;

import com.forgio.dto.request.MachineRequest;
import com.forgio.dto.response.MachineResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.Machine;
import com.forgio.enums.MachineStatus;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.MachineRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;
    private final FactoryRepository factoryRepository;

    /** Every query scoped to TenantContext.getFactoryId() — never client input. */
    @Transactional(readOnly = true)
    public List<MachineResponse> listMachines() {
        UUID factoryId = TenantContext.getFactoryId();
        return machineRepository.findByFactory_FactoryId(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MachineResponse createMachine(MachineRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Machine machine = machineRepository.save(Machine.builder()
                .factory(factory)
                .name(req.name())
                .status(MachineStatus.RUNNING)
                .lastServiceDate(req.lastServiceDate())
                .build());

        return toResponse(machine);
    }

    private MachineResponse toResponse(Machine m) {
        return new MachineResponse(
                m.getMachineId(),
                m.getName(),
                m.getStatus(),
                m.getLastServiceDate());
    }
}