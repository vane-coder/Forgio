package com.forgio.service;

import com.forgio.dto.request.DepartmentRequest;
import com.forgio.dto.response.DepartmentResponse;
import com.forgio.entity.Department;
import com.forgio.entity.Factory;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.DepartmentRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
package com.forgio.service;

import com.forgio.dto.request.DepartmentRequest;
import com.forgio.dto.response.DepartmentResponse;
import com.forgio.entity.Department;
import com.forgio.entity.Factory;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.DepartmentRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        UUID factoryId = TenantContext.getFactoryId();
        return departmentRepository.findByFactory_FactoryId(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Department dept = Department.builder()
                .factory(factory)
                .name(req.name())
                .build();

        return toResponse(departmentRepository.save(dept));
    }

    private DepartmentResponse toResponse(Department d) {
        int workerCount = userRepository.findByDepartment_DeptId(d.getDeptId()).size();
        return new DepartmentResponse(
                d.getDeptId(),
                d.getName(),
                d.getHead() != null ? d.getHead().getName() : null,
                workerCount);
    }
}

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        UUID factoryId = TenantContext.getFactoryId();
        return departmentRepository.findByFactory_FactoryIdOrderByCreatedAtAsc(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Department dept = Department.builder()
                .factory(factory)
                .name(req.name())
                .build();

        return toResponse(departmentRepository.save(dept));
    }

    private DepartmentResponse toResponse(Department d) {
        int workerCount = userRepository.findByDepartment_DeptId(d.getDeptId()).size();
        return new DepartmentResponse(
                d.getDeptId(),
                d.getName(),
                d.getHead() != null ? d.getHead().getName() : null,
                workerCount);
    }
}