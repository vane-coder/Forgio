package com.forgio.repository;

import com.forgio.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByFactory_FactoryId(UUID factoryId);
    Optional<Department> findByDeptIdAndFactory_FactoryId(UUID deptId, UUID factoryId);
}
