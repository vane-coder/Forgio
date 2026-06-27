package com.forgio.service;

import com.forgio.dto.request.PermissionRequest;
import com.forgio.dto.response.PermissionResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.Permission;
import com.forgio.entity.User;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.PermissionRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final FactoryRepository factoryRepository;

    /** Get the permissions for one user (in the manager's factory). */
    @Transactional(readOnly = true)
    public PermissionResponse getForUser(UUID userId) {
        UUID factoryId = TenantContext.getFactoryId();

        // make sure the target user is in THIS factory
        User user = userRepository.findByUserIdAndFactory_FactoryId(userId, factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in this factory"));

        Permission perm = permissionRepository
                .findByUser_UserIdAndFactory_FactoryId(userId, factoryId)
                .orElse(null);

        // if no permissions row exists yet, return all-false defaults
        if (perm == null) {
            return new PermissionResponse(
                    null, user.getUserId(), user.getName(),
                    false, false, false, false, false);
        }
        return toResponse(perm, user);
    }

    /** Assign or update a user's permissions. Creates the row if it doesn't exist. */
    @Transactional
    public PermissionResponse assign(PermissionRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        // the target user must belong to this factory
        User user = userRepository.findByUserIdAndFactory_FactoryId(req.userId(), factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in this factory"));

        // reuse the existing row if there is one, otherwise create a new one
        Permission perm = permissionRepository
                .findByUser_UserIdAndFactory_FactoryId(req.userId(), factoryId)
                .orElseGet(() -> Permission.builder().user(user).factory(factory).build());

        perm.setCanViewReports(req.canViewReports());
        perm.setCanManageWorkers(req.canManageWorkers());
        perm.setCanApproveMarketplace(req.canApproveMarketplace());
        perm.setCanSendNotifications(req.canSendNotifications());
        perm.setCanManageMachines(req.canManageMachines());

        return toResponse(permissionRepository.save(perm), user);
    }

    private PermissionResponse toResponse(Permission p, User user) {
        return new PermissionResponse(
                p.getPermId(),
                user.getUserId(),
                user.getName(),
                p.isCanViewReports(),
                p.isCanManageWorkers(),
                p.isCanApproveMarketplace(),
                p.isCanSendNotifications(),
                p.isCanManageMachines());
    }
}