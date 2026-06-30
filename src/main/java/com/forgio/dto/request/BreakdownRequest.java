package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class BreakdownRequest {
    @NotNull(message = "Machine ID is required")
    private UUID machineId;

    @NotBlank(message = "Issue description cannot be empty")
    private String description;
}
