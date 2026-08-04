package com.forgio.entity;

import com.forgio.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "otp_id")
    private UUID otpId;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(name = "verification_id", unique = true, length = 100)
    private String verificationId;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
