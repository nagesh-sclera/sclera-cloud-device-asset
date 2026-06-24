package io.sclera.config;

import io.sclera.cache.CacheService;
import io.sclera.service.TrustedOriginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class DynamicCorsFilter {

  @Autowired private TrustedOriginService trustedOriginService;
  @Autowired private CacheService cacheService;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    return new CorsConfigurationSource() {

      @Override
      public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

        // Fetch allowed origins from the database based on the request
        CorsConfiguration cacheCorsConfig =
            cacheService.getOriginData("corsConfigurationCache", "corsConfiguration");
        if (cacheCorsConfig == null) {
          cacheCorsConfig = new CorsConfiguration();
          List<String> trustedOrigins = trustedOriginService.getAllOrigin();
          if (trustedOrigins.isEmpty()) {
            trustedOrigins.add("*");
          }
          cacheCorsConfig.setAllowedOriginPatterns(trustedOrigins);
          cacheCorsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
          cacheCorsConfig.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
          cacheCorsConfig.setAllowCredentials(true);
          cacheService.addOriginToCache(
              "corsConfiguration", "corsConfigurationCache", cacheCorsConfig, request);
        }
        return cacheCorsConfig;
      }
    };
  }
}
