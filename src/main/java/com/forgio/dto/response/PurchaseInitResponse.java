package com.forgio.dto.response;

public record PurchaseInitResponse(
        String authorizationUrl,
        String reference
) {}