package com.forgio.dto.response;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        UUID factoryId,
        String name,
        String role
) {}
