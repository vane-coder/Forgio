package com.forgio.dto.response;

import java.time.Instant;

public record AiSuggestionResponse(
        String suggestion,
        String source,      // "AI" if Claude was used, "RULES" if fallback
        Instant generatedAt
) {}