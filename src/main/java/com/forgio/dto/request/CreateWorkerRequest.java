package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateWorkerRequest(
        @NotBlank String name,
        @NotBlank String phone,
        @NotBlank String password,
        String role   // optional: WORKER (default), DEPT_HEAD, or DRIVER
) {}
