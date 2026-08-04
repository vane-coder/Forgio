package com.forgio.service;

import com.forgio.dto.request.*;
import com.forgio.dto.response.AuthResponse;
import com.forgio.dto.response.LoginChallengeResponse;
import com.forgio.dto.response.OtpSentResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.RefreshToken;
import com.forgio.entity.User;
import com.forgio.enums.OtpPurpose;
import com.forgio.enums.SubscriptionPlan;
import com.forgio.enums.UserRole;
import com.forgio.exception.BadRequestException;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.RefreshTokenRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FactoryRepository factoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final OtpService otpService;

    @Value("${forgio.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    // ── Registration (OTP) ──────────────────────────────────────

    @Transactional
    public OtpSentResponse sendRegistrationCode(SendOtpRequest req) {
        if (userRepository.existsByPhone(req.phone())) {
            throw new BadRequestException("A user with this phone number already exists");
        }
        // No User row exists yet at this point, so the email has to come straight
        // from the registration form rather than being looked up.
        String verificationId = otpService.sendOtp(req.phone(), req.email(), OtpPurpose.REGISTRATION);
        return new OtpSentResponse(
                req.email() != null && !req.email().isBlank()
                        ? "Verification code sent to your email"
                        : "Verification code sent to your phone",
                verificationId);
    }

    @Transactional
    public AuthResponse verifyAndRegister(VerifyRegistrationRequest req) {
        otpService.verifyOtpByPhone(req.phone(), req.code(), OtpPurpose.REGISTRATION);

        if (userRepository.existsByPhone(req.phone())) {
            throw new BadRequestException("A user with this phone number already exists");
        }

        Factory factory = factoryRepository.save(Factory.builder()
                .name(req.factoryName())
                .location(req.location())
                .industry(req.industry())
                .plan(SubscriptionPlan.BASIC)
                .active(true)
                .build());

        User manager = userRepository.save(User.builder()
                .factory(factory)
                .name(req.managerName())
                .phone(req.phone())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(UserRole.MANAGER)
                .active(true)
                .build());

        return issueTokens(manager);
    }

    // ── Login (2FA) ─────────────────────────────────────────────

    @Transactional
    public LoginChallengeResponse login(LoginRequest req) {
        User user = userRepository.findByPhone(req.phone())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadRequestException("This account has been deactivated");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (req.fcmToken() != null && !req.fcmToken().isBlank()) {
            user.setFcmToken(req.fcmToken());
            userRepository.save(user);
        }

        String verificationId = otpService.sendOtp(user.getPhone(), user.getEmail(), OtpPurpose.LOGIN);
        String destination = (user.getEmail() != null && !user.getEmail().isBlank())
                ? "Verification code sent to your email"
                : "Verification code sent to your phone";
        return new LoginChallengeResponse(true, verificationId, destination);
    }

    @Transactional
    public AuthResponse verifyLogin(VerifyLoginRequest req) {
        otpService.verifyOtp(req.verificationId(), req.code());

        User user = userRepository.findByPhone(req.phone())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadRequestException("This account has been deactivated");
        }

        return issueTokens(user);
    }

    // ── Password Reset ──────────────────────────────────────────

    @Transactional
    public OtpSentResponse sendPasswordResetCode(SendOtpRequest req) {
        User user = userRepository.findByPhone(req.phone()).orElse(null);
        if (user == null) {
            return new OtpSentResponse("If an account exists, a verification code has been sent", null);
        }
        String verificationId = otpService.sendOtp(user.getPhone(), user.getEmail(), OtpPurpose.PASSWORD_RESET);
        return new OtpSentResponse("If an account exists, a verification code has been sent", verificationId);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        otpService.verifyOtpByPhone(req.phone(), req.code(), OtpPurpose.PASSWORD_RESET);

        User user = userRepository.findByPhone(req.phone())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    // ── Refresh Token (unchanged) ───────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) {
        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(req.refreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found or revoked"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token has expired, please log in again");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(stored.getUser());
    }

    // ── Helpers ─────────────────────────────────────────────────

    private AuthResponse issueTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshTokenStr = UUID.randomUUID() + "." + UUID.randomUUID();

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build());

        return new AuthResponse(
                accessToken,
                refreshTokenStr,
                user.getUserId(),
                user.getFactory().getFactoryId(),
                user.getName(),
                user.getRole().name());
    }
}