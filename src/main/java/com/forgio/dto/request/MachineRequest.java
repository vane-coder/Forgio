package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record MachineRequest(
        @NotBlank String name,
        LocalDate lastServiceDate,
        UUID departmentId   // optional: null = factory-wide (shared) machine
) {}
