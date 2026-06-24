package com.forgio.repository;

import com.forgio.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByUser_UserIdAndFactory_FactoryId(UUID userId, UUID factoryId);
}
