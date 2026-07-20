package com.forgio.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GpsResponse(
        UUID logId,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant recordedAt
) {}