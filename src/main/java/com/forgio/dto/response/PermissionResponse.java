package com.forgio.dto.response;

import java.util.UUID;

public record PermissionResponse(
        UUID permId,
        UUID userId,
        String userName,
        boolean viewReports,
        boolean enterData,
        boolean admin
) {}