package com.forgio.repository;

import com.forgio.entity.OtpVerification;
import com.forgio.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {

    Optional<OtpVerification> findByVerificationIdAndVerifiedFalse(String verificationId);

    long countByPhoneAndPurposeAndCreatedAtAfter(String phone, OtpPurpose purpose, Instant after);

    Optional<OtpVerification> findTopByPhoneAndPurposeAndVerifiedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phone, OtpPurpose purpose, Instant now);

    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
