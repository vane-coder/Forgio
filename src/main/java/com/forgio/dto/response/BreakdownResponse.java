package com.forgio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BreakdownResponse {
    private UUID id;
    private String status;
    private String message;
}
