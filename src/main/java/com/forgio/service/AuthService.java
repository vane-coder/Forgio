package com.forgio.service;

import com.forgio.dto.request.LoginRequest;
import com.forgio.dto.request.RefreshTokenRequest;
import com.forgio.dto.request.RegisterRequest;
import com.forgio.dto.response.AuthResponse;
import com.forgio.entity.Factory;
import com.forgio.entity.RefreshToken;
import com.forgio.entity.User;
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

    @Value("${forgio.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    /**
     * Registers a new factory and its first MANAGER together. This is the only
     * way a brand-new tenant enters the system; all other users are created
     * inside an existing factory by a manager.
     */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
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
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(UserRole.MANAGER)
                .active(true)
                .build());

        return issueTokens(manager);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByPhone(req.phone())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadRequestException("This account has been deactivated");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Update FCM device token so push notifications reach the right device.
        if (req.fcmToken() != null && !req.fcmToken().isBlank()) {
            user.setFcmToken(req.fcmToken());
            userRepository.save(user);
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) {
        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(req.refreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found or revoked"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token has expired, please log in again");
        }

        // Rotate: revoke the old token and issue a fresh pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(stored.getUser());
    }

    // ── helpers ─────────────────────────────────────────────
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
