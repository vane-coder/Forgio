package com.forgio.security;

import com.forgio.entity.User;
import com.forgio.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Runs once per request. If a valid Bearer token is present it:
 *   1. resolves the {@link User},
 *   2. binds the factoryId from the token to {@link TenantContext},
 *   3. populates the Spring Security context.
 *
 * TenantContext is always cleared in finally so threads don't leak tenancy.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (token != null && tokenProvider.isValid(token)) {
                UUID userId    = tokenProvider.getUserId(token);
                UUID factoryId = tokenProvider.getFactoryId(token);

                // Bind tenant from the *verified token*, never from the client body/params.
                TenantContext.setFactoryId(factoryId);

                User user = userRepository.findById(userId).orElse(null);
                if (user != null && user.isActive()
                        && user.getFactory().getFactoryId().equals(factoryId)) {

                    var auth = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
