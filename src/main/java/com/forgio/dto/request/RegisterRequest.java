package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Registers a new manager AND their factory in one step. */
public record RegisterRequest(
        @NotBlank(message = "Manager name is required") String managerName,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank(message = "Factory name is required") String factoryName,
        String location,
        String industry
) {}
