package com.forgio.service;

import com.forgio.dto.request.NotificationRequest;
import com.forgio.dto.response.NotificationResponse;
import com.forgio.entity.Department;
import com.forgio.entity.Factory;
import com.forgio.entity.Notification;
import com.forgio.entity.User;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.DepartmentRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.NotificationRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FactoryRepository factoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    private UUID currentUserId() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUserId();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications() {
        UUID factoryId = TenantContext.getFactoryId();
        return notificationRepository.findByFactory_FactoryIdOrderBySentAtDesc(factoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse send(NotificationRequest req) {
        UUID factoryId = TenantContext.getFactoryId();
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        // reload the sender fresh from the DB inside this transaction
        User sender = userRepository.findById(currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Department targetDept = null;
        if (req.targetDeptId() != null) {
            targetDept = departmentRepository
                    .findByDeptIdAndFactory_FactoryId(req.targetDeptId(), factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target department not found in this factory"));
        }

        Notification notif = Notification.builder()
                .factory(factory)
                .sentBy(sender)
                .targetRole(req.targetRole())
                .targetDept(targetDept)
                .message(req.message())
                .type(req.type())
                .build();

        return toResponse(notificationRepository.save(notif));
    }

    /** Notifications this manager has sent. */
    @Transactional(readOnly = true)
    public List<NotificationResponse> listSentByMe() {
        return notificationRepository
                .findBySentBy_UserIdOrderBySentAtDesc(currentUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponse toResponse(Notification n) {
        User s = n.getSentBy();
        return new NotificationResponse(
                n.getNotifId(),
                n.getMessage(),
                n.getType(),
                n.getTargetRole(),
                n.getTargetDept() != null ? n.getTargetDept().getDeptId() : null,
                s != null ? s.getUserId() : null,
                s != null ? s.getName()   : null,
                n.getSentAt());
    }
}