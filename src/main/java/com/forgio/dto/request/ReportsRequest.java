package com.forgio.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReportsRequest(
        @NotNull(message = "Start date is required") LocalDate startDate,
        @NotNull(message = "End date is required") LocalDate endDate,
        String title   // optional; a default is generated when blank
) {}
