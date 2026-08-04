package com.forgio.service;

import com.forgio.entity.OtpVerification;
import com.forgio.enums.OtpPurpose;
import com.forgio.exception.BadRequestException;
import com.forgio.exception.TooManyRequestsException;
import com.forgio.repository.OtpVerificationRepository;
import com.forgio.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepo;
    private final SmsService smsService;
    private final EmailService emailService;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${forgio.otp.length:6}")
    private int otpLength;

    @Value("${forgio.otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${forgio.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${forgio.otp.rate-limit-count:3}")
    private int rateLimitCount;

    @Value("${forgio.otp.rate-limit-window-minutes:10}")
    private int rateLimitWindowMinutes;

    /**
     * Sends an OTP. Delivered by email when one is available (free); falls back
     * to SMS (costs money per message via Africa's Talking) only when the user
     * has no email on file, so nobody gets locked out of their account.
     */
    @Transactional
    public String sendOtp(String phone, String email, OtpPurpose purpose) {
        Instant windowStart = Instant.now().minus(rateLimitWindowMinutes, ChronoUnit.MINUTES);
        long recentCount = otpRepo.countByPhoneAndPurposeAndCreatedAtAfter(phone, purpose, windowStart);
        if (recentCount >= rateLimitCount) {
            throw new TooManyRequestsException(
                    "Too many verification codes requested. Please try again in a few minutes.");
        }

        String code = generateCode();
        String verificationId = UUID.randomUUID().toString();

        OtpVerification otp = OtpVerification.builder()
                .phone(phone)
                .code(code)
                .purpose(purpose)
                .verificationId(verificationId)
                .expiresAt(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES))
                .build();
        otpRepo.save(otp);

        if (email != null && !email.isBlank()) {
            emailService.send(
                    email,
                    "Your Forgio verification code",
                    String.format("Your Forgio verification code is: %s. It expires in %d minutes.",
                            code, expiryMinutes));
        } else {
            String message = String.format(
                    "Your Forgio verification code is: %s. It expires in %d minutes.", code, expiryMinutes);
            smsService.sendSms(toInternational(phone), message);
        }

        return verificationId;
    }

    @Transactional
    public void verifyOtp(String verificationId, String code) {
        OtpVerification otp = otpRepo.findByVerificationIdAndVerifiedFalse(verificationId)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification"));

        validateAndConsume(otp, code);
    }

    @Transactional
    public void verifyOtpByPhone(String phone, String code, OtpPurpose purpose) {
        OtpVerification otp = otpRepo
                .findTopByPhoneAndPurposeAndVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        phone, purpose, Instant.now())
                .orElseThrow(() -> new BadRequestException("No pending verification found for this phone"));

        validateAndConsume(otp, code);
    }

    private void validateAndConsume(OtpVerification otp, String code) {
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification code has expired");
        }
        if (otp.getAttempts() >= maxAttempts) {
            throw new BadRequestException("Too many failed attempts. Please request a new code.");
        }

        otp.setAttempts(otp.getAttempts() + 1);

        if (!otp.getCode().equals(code)) {
            otpRepo.save(otp);
            throw new BadRequestException("Invalid verification code");
        }

        otp.setVerified(true);
        otpRepo.save(otp);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, otpLength);
        int code = RANDOM.nextInt(bound);
        return String.format("%0" + otpLength + "d", code);
    }

    private String toInternational(String phone) {
        if (phone.startsWith("0")) {
            return "+233" + phone.substring(1);
        }
        if (phone.startsWith("+")) {
            return phone;
        }
        return "+233" + phone;
    }
}