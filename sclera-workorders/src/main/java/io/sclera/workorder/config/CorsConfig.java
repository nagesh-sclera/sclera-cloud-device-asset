package io.sclera.workorder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Preserves the monolith's wide-open CORS policy.
 * The monolith MaximoController had {@code @CrossOrigin(origins = "*", allowedHeaders = "*")}
 * on the class; this lifts it to a global config so it can also be reached
 * directly during local development (bypassing the gateway).
 *
 * In production, prefer the gateway's CORS layer and tighten this — but for
 * the literal behavior-preservation goal, the wildcard is reproduced here.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
