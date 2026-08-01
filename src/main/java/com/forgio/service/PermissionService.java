package com.forgio.service;

import com.forgio.dto.request.CreateWorkerRequest;
import com.forgio.dto.request.PermissionRequest;
import com.forgio.dto.response.PermissionResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.Permission;
import com.forgio.entity.User;
import com.forgio.enums.UserRole;
import com.forgio.exception.BadRequestException;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.PermissionRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final FactoryRepository factoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PermissionResponse getForUser(UUID userId) {
        UUID factoryId = TenantContext.getFactoryId();

        User user = userRepository.findByUserIdAndFactory_FactoryId(userId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in this factory"));

        Permission perm = permissionRepository
                .findByUser_UserIdAndFactory_FactoryId(userId, factoryId)
                .orElse(null);

        if (perm == null) {
            return new PermissionResponse(
                    null, user.getUserId(), user.getName(), user.getRole().name(),
                    false, false, false);
        }
        return toResponse(perm, user);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listAllWithPermissions() {
        UUID factoryId = TenantContext.getFactoryId();
        return userRepository.findByFactory_FactoryId(factoryId).stream()
                .map(user -> {
                    Permission perm = permissionRepository
                            .findByUser_UserIdAndFactory_FactoryId(user.getUserId(), factoryId)
                            .orElse(null);
                    if (perm == null) {
                        return new PermissionResponse(
                                null, user.getUserId(), user.getName(), user.getRole().name(),
                                false, false, false);
                    }
                    return toResponse(perm, user);
                })
                .toList();
    }

    @Transactional
    public PermissionResponse createWorker(CreateWorkerRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        if (userRepository.existsByPhone(req.phone())) {
            throw new BadRequestException("A user with this phone number already exists");
        }

        UserRole role = UserRole.WORKER;
        if (req.role() != null && !req.role().isBlank()) {
            try {
                UserRole requested = UserRole.valueOf(req.role().trim().toUpperCase());
                // A manager may only create non-privileged staff roles.
                if (requested == UserRole.WORKER || requested == UserRole.DEPT_HEAD || requested == UserRole.DRIVER) {
                    role = requested;
                } else {
                    throw new BadRequestException("Cannot create a user with role " + requested);
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + req.role());
            }
        }

        User user = userRepository.save(User.builder()
                .factory(factory)
                .name(req.name())
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(role)
                .active(true)
                .build());

        return new PermissionResponse(
                null, user.getUserId(), user.getName(), user.getRole().name(),
                false, false, false);
    }

    @Transactional
    public PermissionResponse assign(PermissionRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        User user = userRepository.findByUserIdAndFactory_FactoryId(req.userId(), factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in this factory"));

        Permission perm = permissionRepository
                .findByUser_UserIdAndFactory_FactoryId(req.userId(), factoryId)
                .orElseGet(() -> Permission.builder().user(user).factory(factory).build());

        perm.setViewReports(req.viewReports());
        perm.setEnterData(req.enterData());
        perm.setAdmin(req.admin());

        return toResponse(permissionRepository.save(perm), user);
    }

    private PermissionResponse toResponse(Permission p, User user) {
        return new PermissionResponse(
                p.getPermId(),
                user.getUserId(),
                user.getName(),
                user.getRole().name(),
                p.isViewReports(),
                p.isEnterData(),
                p.isAdmin());
    }
}