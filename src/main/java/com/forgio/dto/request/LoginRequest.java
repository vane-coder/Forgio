package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String phone,
        @NotBlank String password,
        String fcmToken    // optional: device token updated on login
) {}
