package com.forgio.dto.response;

import com.forgio.enums.MachineStatus;

import java.time.LocalDate;
import java.util.UUID;

public record MachineResponse(
        UUID machineId,
        String name,
        MachineStatus status,
        LocalDate lastServiceDate
) {}