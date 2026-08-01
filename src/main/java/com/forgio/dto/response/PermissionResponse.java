package com.forgio.dto.response;

import java.util.UUID;

public record PermissionResponse(
        UUID permId,
        UUID userId,
        String userName,
        String role,
        boolean viewReports,
        boolean enterData,
        boolean admin
) {}
