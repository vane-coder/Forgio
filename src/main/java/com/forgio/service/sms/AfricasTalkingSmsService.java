package com.forgio.service.sms;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@ConditionalOnProperty(name = "forgio.sms.provider", havingValue = "africastalking", matchIfMissing = true)
public class AfricasTalkingSmsService implements SmsService {

    @Value("${forgio.sms.africastalking.username}")
    private String username;

    @Value("${forgio.sms.africastalking.api-key}")
    private String apiKey;

    @Value("${forgio.sms.africastalking.sender-id:}")
    private String senderId;

    private WebClient webClient;

    @PostConstruct
    void init() {
        String baseUrl = "sandbox".equalsIgnoreCase(username)
                ? "https://api.sandbox.africastalking.com"
                : "https://api.africastalking.com";

        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apiKey", apiKey)
                .defaultHeader("Accept", "application/json")
                .build();

        log.info("Africa's Talking SMS service initialized (username={}, env={})",
                username, "sandbox".equalsIgnoreCase(username) ? "sandbox" : "production");
    }

    @Override
    public void sendSms(String toPhone, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("SMS not sent to {} — AT_API_KEY is not configured", toPhone);
            throw new RuntimeException("SMS service is not configured: missing AT_API_KEY");
        }

        StringBuilder form = new StringBuilder();
        form.append("username=").append(username)
            .append("&to=").append(toPhone)
            .append("&message=").append(message);

        if (senderId != null && !senderId.isBlank()) {
            form.append("&from=").append(senderId);
        }

        try {
            String response = webClient.post()
                    .uri("/version1/messaging")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(form.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("SMS sent to {}, response: {}", toPhone, response);
        } catch (Exception ex) {
            log.error("Failed to send SMS to {}: {}", toPhone, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send SMS: " + ex.getMessage(), ex);
        }
    }
}
