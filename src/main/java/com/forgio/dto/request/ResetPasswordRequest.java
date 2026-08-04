package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Verification code is required") @Size(min = 6, max = 6) String code,
        @NotBlank @Size(min = 6, message = "New password must be at least 6 characters") String newPassword
) {}
