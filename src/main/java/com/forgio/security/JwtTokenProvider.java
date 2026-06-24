package com.forgio.security;

import com.forgio.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWTs. The token embeds userId, factoryId, role and
 * permissions so that the tenant can be resolved on every request without a
 * database hit — and crucially, without trusting any client-supplied factoryId.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${forgio.jwt.secret}")
    private String secret;

    @Value("${forgio.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("factoryId", user.getFactory().getFactoryId().toString())
                .claim("role", user.getRole().name())
                .claim("name", user.getName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public UUID getUserId(String token)    { return UUID.fromString(parse(token).getSubject()); }
    public UUID getFactoryId(String token) { return UUID.fromString(parse(token).get("factoryId", String.class)); }
    public String getRole(String token)    { return parse(token).get("role", String.class); }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
