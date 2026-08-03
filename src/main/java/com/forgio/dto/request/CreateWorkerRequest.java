package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateWorkerRequest(
        @NotBlank String name,
        @NotBlank String phone,
        @NotBlank String password,
        String role,          // optional: WORKER (default), DEPT_HEAD, or DRIVER
        UUID departmentId     // optional: department this worker belongs to
) {}
