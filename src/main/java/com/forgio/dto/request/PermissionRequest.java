package com.forgio.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PermissionRequest(
        @NotNull(message = "User id is required") UUID userId,
        boolean canViewReports,
        boolean canManageWorkers,
        boolean canApproveMarketplace,
        boolean canSendNotifications,
        boolean canManageMachines
) {}