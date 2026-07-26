-- ============================================================
-- V2: OTP verification codes for SMS-based authentication
-- ============================================================

CREATE TABLE otp_verifications (
    otp_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone           VARCHAR(20)  NOT NULL,
    code            VARCHAR(6)   NOT NULL,
    purpose         VARCHAR(30)  NOT NULL,   -- REGISTRATION, LOGIN, PASSWORD_RESET
    verification_id VARCHAR(100) UNIQUE,     -- opaque token linking request to verify step
    attempts        INT          NOT NULL DEFAULT 0,
    verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_phone_purpose     ON otp_verifications(phone, purpose);
CREATE INDEX idx_otp_verification_id   ON otp_verifications(verification_id);
CREATE INDEX idx_otp_expires_at        ON otp_verifications(expires_at);
