package io.sclera.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Enables Spring caching and configures the Caffeine-backed cache manager for the service.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Provides a Caffeine cache manager with the {@code inspection_activity_signout} cache,
     * whose entries expire 60 minutes after being written.
     *
     * @return the configured Caffeine cache manager
     */
    @Bean
    public CaffeineCacheManager cacheManager() {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("inspection_activity_signout");

        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(60,TimeUnit.MINUTES)

        );

        return cacheManager;
    }
}