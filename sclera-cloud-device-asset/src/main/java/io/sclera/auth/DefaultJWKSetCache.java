package io.sclera.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCache;

public class DefaultJWKSetCache implements JWKSetCache {

    private JWKSet jwkSet;
    private long lastUpdateTime;
    private final long cacheTimeMillis;

    public DefaultJWKSetCache(long cacheTimeMillis) {
        this.cacheTimeMillis = cacheTimeMillis;
    }

    @Override
    public synchronized void put(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public synchronized JWKSet get() {
        if (requiresRefresh()) {
            return null;
        }
        return jwkSet;
    }

    @Override
    public synchronized boolean requiresRefresh() {
        return jwkSet == null || (System.currentTimeMillis() - lastUpdateTime) > cacheTimeMillis;
    }


}