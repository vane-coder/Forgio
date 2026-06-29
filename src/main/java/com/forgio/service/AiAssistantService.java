package com.forgio.service;

import com.forgio.dto.response.AiSuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAssistantService {

    private final FactoryDataSummaryService summaryService;

    @Value("${forgio.claude.api-key:}")
    private String claudeApiKey;

    @Value("${forgio.claude.model:claude-sonnet-4-6}")
    private String claudeModel;

    public AiSuggestionResponse getSuggestion() {
        FactoryDataSummaryService.Summary summary = summaryService.build();

        // If no API key is configured, use the rules-based fallback.
        if (claudeApiKey == null || claudeApiKey.isBlank()) {
            return new AiSuggestionResponse(rulesBasedSuggestion(summary), "RULES", Instant.now());
        }

        try {
            String aiText = callClaude(summary.text());
            return new AiSuggestionResponse(aiText, "AI", Instant.now());
        } catch (Exception e) {
            log.warn("Claude call failed, using fallback: {}", e.getMessage());
            return new AiSuggestionResponse(rulesBasedSuggestion(summary), "RULES", Instant.now());
        }
    }

    // ----- Rules-based fallback (no API needed) -----
    private String rulesBasedSuggestion(FactoryDataSummaryService.Summary s) {
        if (!s.lowStockMaterials().isEmpty()) {
            return "Restock soon: " + String.join(", ", s.lowStockMaterials())
                    + " are at or below reorder level. Order more to avoid a production halt.";
        }
        if (s.machinesStopped() > 0) {
            return s.machinesStopped() + " machine(s) are currently stopped. "
                    + "Investigate and schedule repairs to keep production on track.";
        }
        if (s.machinesInMaintenance() > 0) {
            return s.machinesInMaintenance() + " machine(s) are under maintenance. "
                    + "Plan production around reduced capacity until they are back online.";
        }
        if (s.productionEntriesLast7Days() == 0) {
            return "No production was recorded in the last 7 days. "
                    + "Check that workers are submitting their daily entries.";
        }
        return "Operations look healthy: " + s.productionEntriesLast7Days()
                + " production entries this week and no low-stock or machine issues. "
                + "Keep monitoring stock levels and machine status.";
    }

    // ----- Real Claude call -----
    private String callClaude(String factorySummary) {
        WebClient client = WebClient.create("https://api.anthropic.com");

        String prompt = "You are an operations advisor for a small factory in Ghana. "
                + "Based on this data, give ONE short, specific, actionable suggestion "
                + "(2-3 sentences, plain language) for what the manager should do next:\n\n"
                + factorySummary;

        Map<String, Object> body = Map.of(
                "model", claudeModel,
                "max_tokens", 300,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map<String, Object> response = client.post()
                .uri("/v1/messages")
                .header("x-api-key", claudeApiKey)
                .header("anthropic-version", "2023-06-01")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Claude returns content as a list of blocks; pull the first text block.
        if (response != null && response.get("content") instanceof List<?> content && !content.isEmpty()
                && content.get(0) instanceof Map<?, ?> first && first.get("text") != null) {
            return first.get("text").toString().trim();
        }
        throw new IllegalStateException("Unexpected Claude response format");
    }
}