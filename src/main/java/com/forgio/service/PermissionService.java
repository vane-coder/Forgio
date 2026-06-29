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
                    null, user.getUserId(), user.getName(),
                    false, false, false);
        }
        return toResponse(perm, user);
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
                p.isViewReports(),
                p.isEnterData(),
                p.isAdmin());
    }
}