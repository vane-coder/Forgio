package com.forgio.controller;

import com.forgio.dto.request.*;
import com.forgio.dto.response.AuthResponse;
import com.forgio.dto.response.LoginChallengeResponse;
import com.forgio.dto.response.OtpSentResponse;
import com.forgio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Registration (OTP) ──────────────────────────────────────

    @PostMapping("/register/send-code")
    public ResponseEntity<OtpSentResponse> sendRegistrationCode(@Valid @RequestBody SendOtpRequest req) {
        return ResponseEntity.ok(authService.sendRegistrationCode(req));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<AuthResponse> verifyAndRegister(@Valid @RequestBody VerifyRegistrationRequest req) {
        return ResponseEntity.ok(authService.verifyAndRegister(req));
    }

    // ── Login (2FA) ─────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<LoginChallengeResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponse> verifyLogin(@Valid @RequestBody VerifyLoginRequest req) {
        return ResponseEntity.ok(authService.verifyLogin(req));
    }

    // ── Password Reset ──────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<OtpSentResponse> forgotPassword(@Valid @RequestBody SendOtpRequest req) {
        return ResponseEntity.ok(authService.sendPasswordResetCode(req));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
    }

    // ── Refresh Token (unchanged) ───────────────────────────────

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }
}
