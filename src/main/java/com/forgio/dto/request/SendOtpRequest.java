package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendOtpRequest(
        @NotBlank(message = "Phone is required") String phone
) {}
