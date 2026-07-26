package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyLoginRequest(
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Verification code is required") @Size(min = 6, max = 6) String code,
        @NotBlank(message = "Verification ID is required") String verificationId
) {}
