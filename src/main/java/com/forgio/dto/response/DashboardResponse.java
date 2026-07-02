package com.forgio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private UUID factoryId;
    private String systemStatus;
    private long activeIssuesCount;
    private String alertMessage;
}
