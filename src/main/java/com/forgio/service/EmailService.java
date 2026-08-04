package com.forgio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${forgio.brevo.api-key:}")
    private String apiKey;

    @Value("${forgio.brevo.from-email:}")
    private String fromEmail;

    @Value("${forgio.brevo.from-name:Forgio}")
    private String fromName;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.brevo.com/v3")
            .build();

    public void send(String to, String subject, String body) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Brevo API key is missing! Check your BREVO_API_KEY environment variable on Render.");
            throw new IllegalStateException("Email service is not configured: Missing BREVO_API_KEY");
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            log.error("Brevo From-Email is missing! Check your BREVO_FROM_EMAIL environment variable on Render.");
            throw new IllegalStateException("Email service is not configured: Missing BREVO_FROM_EMAIL");
        }

        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", fromName, "email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body
        );

        try {
            webClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> 
                        clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("Brevo API Error (Status {}): {}", clientResponse.statusCode(), errorBody);
                                return Mono.error(new RuntimeException("Brevo API error: " + errorBody));
                            })
                    )
                    .toBodilessEntity()
                    .block();

            log.info("Successfully sent email to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw e;
        }
    }
}