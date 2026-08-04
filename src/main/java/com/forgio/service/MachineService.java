package com.forgio.service;

import com.forgio.dto.request.MachineRequest;
import com.forgio.dto.response.MachineResponse;
import com.forgio.entity.Department;
import com.forgio.entity.Factory;
import com.forgio.entity.Machine;
import com.forgio.entity.User;
import com.forgio.enums.MachineStatus;
import com.forgio.enums.UserRole;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.DepartmentRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.MachineRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;
    private final FactoryRepository factoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

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

        User me = currentUser();
        // Managers/dept-heads see the whole factory; workers see their own
        // department plus factory-wide (unassigned) machines only.
        List<Machine> machines;
        if (me.getRole() == UserRole.MANAGER || me.getRole() == UserRole.SYSTEM_ADMIN
                || me.getRole() == UserRole.DEPT_HEAD) {
            machines = machineRepository.findByFactory_FactoryId(factoryId);
        } else {
            UUID deptId = me.getDepartment() != null ? me.getDepartment().getDeptId() : null;
            machines = machineRepository.findVisibleToDepartment(factoryId, deptId);
        }
        return machines.stream().map(this::toResponse).toList();
    }

    @Transactional
    public MachineResponse createMachine(MachineRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Department department = null;
        if (req.departmentId() != null) {
            department = departmentRepository
                    .findByDeptIdAndFactory_FactoryId(req.departmentId(), factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found in this factory"));
        }

        Machine machine = machineRepository.save(Machine.builder()
                .factory(factory)
                .department(department)
                .name(req.name())
                .status(MachineStatus.RUNNING)
                .lastServiceDate(req.lastServiceDate())
                .build());

        return toResponse(machine);
    }

    private User currentUser() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private MachineResponse toResponse(Machine m) {
        Department d = m.getDepartment();
        return new MachineResponse(
                m.getMachineId(),
                m.getName(),
                m.getStatus(),
                m.getLastServiceDate(),
                d != null ? d.getDeptId() : null,
                d != null ? d.getName()   : null);
    }
}