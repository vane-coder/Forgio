package com.forgio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportsRequest {
    @NotBlank(message = "Report title is required")
    private String title;

    @NotBlank(message = "Report type must be WEEKLY or MONTHLY")
    private String reportType;
}
