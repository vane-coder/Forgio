package com.forgio.dto.response;

import com.forgio.enums.NotificationType;
import com.forgio.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notifId,
        String message,
        NotificationType type,
        UserRole targetRole,
        UUID targetDeptId,
        String targetDeptName,
        UUID sentById,
        String sentByName,
        Instant sentAt
) {}