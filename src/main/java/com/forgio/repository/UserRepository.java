package com.forgio.repository;

import com.forgio.entity.User;
import com.forgio.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);

    // Tenant-scoped reads
    List<User> findByFactory_FactoryId(UUID factoryId);
    Optional<User> findByUserIdAndFactory_FactoryId(UUID userId, UUID factoryId);
    List<User> findByFactory_FactoryIdAndRole(UUID factoryId, UserRole role);
    List<User> findByDepartment_DeptId(UUID deptId);
}
