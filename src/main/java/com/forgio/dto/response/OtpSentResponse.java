package com.forgio.dto.response;

public record OtpSentResponse(
        String message,
        String verificationId
) {}
