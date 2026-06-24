package io.sclera.config;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

//    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public JwtDecoder jwtDecoder(JWTProcessor jwtProcessor, OAuth2TokenValidator<Jwt> jwtValidator) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        // Create a validator that combines default validators with the custom validator provided
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), jwtValidator);
        // Set the validator for the decoder
        decoder.setJwtValidator(validator);
        return decoder;
    }

    // Creates a bean for TenantJWSKeySelector
    @Bean
    public TenantJWSKeySelector tenantJWSKeySelector() {
        return new TenantJWSKeySelector();
    }

    // Creates a bean for JWTProcessor
    @Bean
    public JWTProcessor jwtProcessor() {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        // Set the JWTClaimsSetAwareJWSKeySelector for the JWTProcessor
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(tenantJWSKeySelector());
        return jwtProcessor;
    }

    // Creates a bean for TenantJwtIssuerValidator
    @Bean
    public TenantJwtIssuerValidator tenantJwtIssuerValidator() {
        return new TenantJwtIssuerValidator();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().ignoringRequestMatchers("/**");

        http
                .cors(Customizer.withDefaults());
        // Request mapping configuration
        http
                .authorizeHttpRequests((authorization) -> authorization
                                .requestMatchers(
                                        new AntPathRequestMatcher("/**/getAllAssetTypeNames"),
                                        new AntPathRequestMatcher("/**/deleteUsersByMasterUser"),
                                        new AntPathRequestMatcher("/**/createMasterUser"),
                                        new AntPathRequestMatcher("/api/portal/**"),
                                        new AntPathRequestMatcher("/**/vdms/sync"),
                                        new AntPathRequestMatcher("/**/proxyProfile"),
                                        new AntPathRequestMatcher("/**/activateVdms"),
                                        new AntPathRequestMatcher("/**/getOrgIdByQrCodeId"),
                                        new AntPathRequestMatcher("/ws/**"),
                                        new AntPathRequestMatcher("/getvdmsversion"),
                                        new AntPathRequestMatcher("/**/getQrCodeDetailsByQrCodeId"),
                                        new AntPathRequestMatcher("/**/getVdmsAccessTokenByVdmsIdAndPassword"),
                                        new AntPathRequestMatcher("/**/p2p/session"),
                                        new AntPathRequestMatcher("/api/websocket"),
                                        new AntPathRequestMatcher("/**/getProxyProfileByVdmsId"),
                                        new AntPathRequestMatcher("/api/touchscreen/qrCode/email"),
                                        new AntPathRequestMatcher("/**/getLanguageByEmail"),
                                        new AntPathRequestMatcher("/**/createUserByMasterUserOnboarding"),
                                        new AntPathRequestMatcher("/**/multiTenantCheck"),
                                        new AntPathRequestMatcher("/**/multiTenantVdmsInfo"),
                                        new AntPathRequestMatcher("/**/logError"),
                                        new AntPathRequestMatcher("/api/touchscreen/validatePreSignLink"),
                                        new AntPathRequestMatcher("/api/assetVersion"),
                                        new AntPathRequestMatcher("/**/getClientQRCodeProxyProfileByVdmsId"),
                                        new AntPathRequestMatcher("/**/getClientNfcProxyProfileByVdmsId"),
                                        new AntPathRequestMatcher("/**/vdms-keys/disk_key"),
                                        new AntPathRequestMatcher("/**/vdms-keys/sqlite_key"),
                                        new AntPathRequestMatcher("/**/vdms-keys/mysql_key"),
                                        new AntPathRequestMatcher("/**/getClientBarCodeProxyProfileByVdmsId"),
                                        new AntPathRequestMatcher("/**/getOrgIdByBarCodeId"),

                                        //Production image configuration
                                        new AntPathRequestMatcher("/**/floor/images/**"),
                                        new AntPathRequestMatcher("/sclera-resource/qr-code/**"),
                                        new AntPathRequestMatcher("/sclera-resource/app/**"),
                                        new AntPathRequestMatcher("/sclera-resource/ioc/**"),
                                        new AntPathRequestMatcher("/**/sclera-resource/profile/images/**"),
                                        new AntPathRequestMatcher("/sclera-resource/vdms/**"),
                                        new AntPathRequestMatcher("/sclera-resource/organisation/images/**"),
                                        new AntPathRequestMatcher("/sclera-resource/sclera-docs/**"),
                                        new AntPathRequestMatcher("/sclera-resource/sclera.jar"),
                                        new AntPathRequestMatcher("/sclera-resource/category/**"),
                                        new AntPathRequestMatcher("/sclera-resource/subCategory/**"),
                                        new AntPathRequestMatcher("/sclera-resource/sensor/**"),
                                        new AntPathRequestMatcher("/sclera-resource/assetType/**"),
                                        new AntPathRequestMatcher("/sclera-resource/procedure/**"),

                                        //UAT image configuration
                                        new AntPathRequestMatcher("/sclera-uat-resource/qr-code/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/app/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/ioc/**"),
                                        new AntPathRequestMatcher("/**/sclera-uat-resource/profile/images/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/vdms/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/organisation/images/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/sclera-docs/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/sclera.jar"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/category/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/subCategory/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/sensor/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/assetType/**"),
                                        new AntPathRequestMatcher("/sclera-uat-resource/procedure/**"),

                                        //QA image configuration
                                        new AntPathRequestMatcher("/sclera-qa-resource/qr-code/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/app/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/ioc/**"),
                                        new AntPathRequestMatcher("/**/sclera-qa-resource/profile/images/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/vdms/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/organisation/images/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/sclera-docs/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/sclera.jar"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/category/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/subCategory/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/sensor/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/assetType/**"),
                                        new AntPathRequestMatcher("/sclera-qa-resource/procedure/**"),

                                        //DEV image configuration
                                        new AntPathRequestMatcher("/sclera-dev-resource/qr-code/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/app/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/ioc/**"),
                                        new AntPathRequestMatcher("/**/sclera-dev-resource/profile/images/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/vdms/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/organisation/images/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/sclera-docs/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/sclera.jar"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/category/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/subCategory/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/sensor/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/assetType/**"),
                                        new AntPathRequestMatcher("/sclera-dev-resource/procedure/**")

                                ).permitAll()
                                .requestMatchers("/users/**").authenticated()
                                .anyRequest()
                                .authenticated()

                );
        // Session configuration
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        // Add the Authentication Manager
        http
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder(jwtProcessor(), tenantJwtIssuerValidator()))));

        // To add security headers
        http
                .headers(header -> header
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
                        )
                        .permissionsPolicy(permissions -> permissions
                                .policy("camera=(self), microphone=(self), geolocation=(self)")
                        ).and()
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; img-src *; object-src *")
                        )
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(63072000)
                        )
                        .addHeaderWriter((request, response) -> {
                            response.setHeader("Strict-Transport-Security", "max-age=63072000 ; includeSubDomains");
                        })
                );

//        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}