package com.forgio.dto.response;

import java.util.UUID;

public record PermissionResponse(
        UUID permId,
        UUID userId,
        String userName,
        String role,
        UUID departmentId,
        String departmentName,
        boolean viewReports,
        boolean enterData,
        boolean admin
) {}
