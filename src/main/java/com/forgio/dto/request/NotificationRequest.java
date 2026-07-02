package com.forgio.dto.request;

import com.forgio.enums.NotificationType;
import com.forgio.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NotificationRequest(
        @NotBlank(message = "Message is required") String message,
        @NotNull(message = "Type is required") NotificationType type,
        UserRole targetRole,
        UUID targetDeptId
) {}