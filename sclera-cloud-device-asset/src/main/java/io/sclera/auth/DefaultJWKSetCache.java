package io.sclera.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCache;

/**
 * Time-based in-memory cache for a {@link JWKSet}. Stores a single key set and
 * treats it as expired once the configured cache lifetime has elapsed since the
 * last update.
 */
public class DefaultJWKSetCache implements JWKSetCache {

    private JWKSet jwkSet;
    private long lastUpdateTime;
    private final long cacheTimeMillis;

    public DefaultJWKSetCache(long cacheTimeMillis) {
        this.cacheTimeMillis = cacheTimeMillis;
    }

    /**
     * Stores the given key set and records the current time as its update timestamp.
     */
    @Override
    public synchronized void put(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Returns the cached key set, or {@code null} if it is absent or expired.
     */
    @Override
    public synchronized JWKSet get() {
        if (requiresRefresh()) {
            return null;
        }
        return jwkSet;
    }

    /**
     * Indicates whether the cache is empty or has exceeded its configured lifetime.
     */
    @Override
    public synchronized boolean requiresRefresh() {
        return jwkSet == null || (System.currentTimeMillis() - lastUpdateTime) > cacheTimeMillis;
    }


}