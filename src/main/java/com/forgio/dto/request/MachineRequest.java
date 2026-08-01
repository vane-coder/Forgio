package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MachineRequest(
        @NotBlank String name,
        LocalDate lastServiceDate
) {}
