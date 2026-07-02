package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BranchRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Location is required") String location
) {}
