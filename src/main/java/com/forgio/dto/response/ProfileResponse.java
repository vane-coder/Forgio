package com.forgio.dto.response;

import com.forgio.enums.UserRole;

import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String name,
        String phone,
        UserRole role,
        UUID factoryId,
        String factoryName,
        UUID departmentId,
        String departmentName
) {}