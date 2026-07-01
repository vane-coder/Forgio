package com.forgio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReportsResponse {
    private UUID id;
    private String status;
    private String message;
}
