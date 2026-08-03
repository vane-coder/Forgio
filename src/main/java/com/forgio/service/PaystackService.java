package com.forgio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class PaystackService {

    @Value("${forgio.paystack.secret-key:}")
    private String secretKey;

    @Value("${forgio.paystack.callback-url:}")
    private String callbackUrl;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.paystack.co")
            .build();

    /** Starts a transaction. Amount is passed in GHS; Paystack wants pesewas (GHS * 100). */
    public PaystackInitResult initialize(String email, BigDecimal amountGhs, String reference) {
        Map<String, Object> body = Map.of(
                "email", email,
                "amount", amountGhs.multiply(BigDecimal.valueOf(100)).intValue(),
                "reference", reference,
                "callback_url", callbackUrl
        );

        Map<String, Object> response = webClient.post()
                .uri("/transaction/initialize")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        return new PaystackInitResult((String) data.get("authorization_url"), (String) data.get("reference"));
    }

    /** Re-checks the transaction directly with Paystack. Never trust the redirect query params alone. */
    public boolean verify(String reference) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/transaction/verify/{reference}", reference)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            return data != null && "success".equals(data.get("status"));
        } catch (Exception e) {
            log.warn("Paystack verify failed for {}: {}", reference, e.getMessage());
            return false;
        }
    }

    public record PaystackInitResult(String authorizationUrl, String reference) {}
}