package com.forgio.dto.response;

import java.util.UUID;

public record PermissionResponse(
        UUID permId,
        UUID userId,
        String userName,
        boolean canViewReports,
        boolean canManageWorkers,
        boolean canApproveMarketplace,
        boolean canSendNotifications,
        boolean canManageMachines
) {}