package io.sclera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Defines the web security filter chain used under the {@code docker} profile, disabling CSRF and
 * CORS and permitting all requests without authentication.
 */
@Configuration
@EnableWebSecurity
@Profile("docker")
public class DockerSecurityConfig {

    /**
     * Provides the security filter chain that disables CSRF and CORS and permits every request.
     *
     * @param http the HTTP security builder
     * @return the configured security filter chain
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    public SecurityFilterChain dockerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
