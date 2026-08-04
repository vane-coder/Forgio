package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateWorkerRequest(
        @NotBlank String name,
        @NotBlank String phone,
        String email,          // optional: used to deliver OTP codes instead of SMS
        @NotBlank String password,
        String role,           // optional: DEPT_HEAD (default) or DRIVER
        UUID departmentId      // optional: department this worker belongs to
) {}