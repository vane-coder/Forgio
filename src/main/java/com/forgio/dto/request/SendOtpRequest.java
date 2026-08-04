package com.forgio.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendOtpRequest(
        @NotBlank(message = "Phone is required") String phone,
        @Email String email   // optional — only used for registration where no User row exists yet
) {}