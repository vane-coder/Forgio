package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Phone is required") String phone
) {}