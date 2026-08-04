package com.forgio.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRegistrationRequest(
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Email is required") @Email String email,
        @NotBlank(message = "Verification code is required") @Size(min = 6, max = 6) String code,
        @NotBlank(message = "Manager name is required") String managerName,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank(message = "Factory name is required") String factoryName,
        String location,
        String industry
) {}