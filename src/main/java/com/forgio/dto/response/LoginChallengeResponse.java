package com.forgio.dto.response;

public record LoginChallengeResponse(
        boolean otpRequired,
        String verificationId,
        String message
) {}
