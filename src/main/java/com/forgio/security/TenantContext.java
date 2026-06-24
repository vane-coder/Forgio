package com.forgio.security;

import java.util.UUID;

/**
 * Holds the current request's tenant ({@code factoryId}) in a ThreadLocal.
 *
 * <p>The {@link JwtAuthenticationFilter} populates this on every authenticated
 * request, reading the factoryId <strong>from the verified JWT only</strong> —
 * never from client input. Services read it via {@link #getFactoryId()} to scope
 * every query, guaranteeing one factory can never touch another's data.
 *
 * <p>The filter MUST call {@link #clear()} in a finally block so the value does
 * not leak across pooled threads.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_FACTORY = new ThreadLocal<>();

    private TenantContext() {}

    public static void setFactoryId(UUID factoryId) {
        CURRENT_FACTORY.set(factoryId);
    }

    public static UUID getFactoryId() {
        UUID id = CURRENT_FACTORY.get();
        if (id == null) {
            throw new IllegalStateException("No tenant (factoryId) bound to the current request");
        }
        return id;
    }

    public static boolean hasTenant() {
        return CURRENT_FACTORY.get() != null;
    }

    public static void clear() {
        CURRENT_FACTORY.remove();
    }
}
