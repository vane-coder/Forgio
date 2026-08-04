package com.forgio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DashboardResponse {
    private UUID factoryId;
    private String systemStatus;
    private long activeIssuesCount;
    private String alertMessage;
    private List<TodayProductionLine> todayProduction;

    @Data
    @AllArgsConstructor
    public static class TodayProductionLine {
        private String productName;
        private BigDecimal totalQuantity;
        private String departmentName; // null if factory-wide/unassigned
    }
}