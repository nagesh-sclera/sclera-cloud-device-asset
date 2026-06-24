package io.sclera.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.access.channel.ChannelProcessingFilter;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//
//@SuppressWarnings("deprecation")
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//
//    @Autowired
//    private UserDetailsService jwtUserDetailsService;
//
//    @Autowired
//    private JwtRequestFilter jwtRequestFilter;
//
//
//    @Override
//    @Bean
//    protected UserDetailsService userDetailsService() {
//        return super.userDetailsService();
//    }
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(jwtUserDetailsService);
//    }
//
//
//    @Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity.cors().and()
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/ws/**", "/authenticate","/getcountrydatabycountryname","/getcitydatabycityname",
//                        "/user/**/vdms/**/docker", "/**/getdevice","/**/getassociateglobalchecklist",
//                        "/**/upsertrecordchecklist","/**/getroomstatus","/**/getglobalchecklistitems").permitAll()
//                .anyRequest().permitAll()
//                .and()
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return  NoOpPasswordEncoder.getInstance();
//    }
//
//    @Override
//    @Bean
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//
//}

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.sclera.auth.TenantJWSKeySelector;
import io.sclera.auth.TenantJwtIssuerValidator;
import io.sclera.utils.Utils;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Configures stateless OAuth2 resource-server security (active outside the {@code docker} profile),
 * validating multi-tenant JWTs and permitting requests from local or bridge-subnet clients while
 * applying security response headers.
 */
@Configuration
@EnableWebSecurity
@org.springframework.context.annotation.Profile("!docker")
public class WebSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private Utils utils;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    /**
     * Provides a JWT decoder backed by the given processor, combining the default validators with
     * the tenant issuer validator.
     *
     * @param jwtProcessor the Nimbus JWT processor
     * @param jwtValidator the additional tenant token validator
     * @return the configured JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(JWTProcessor<SecurityContext> jwtProcessor, OAuth2TokenValidator<Jwt> jwtValidator) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), jwtValidator);
        decoder.setJwtValidator(validator);
        return decoder;
    }


    /**
     * Provides the tenant-aware JWS key selector used to resolve signing keys per tenant.
     *
     * @return the tenant JWS key selector
     */
    @Bean
    public TenantJWSKeySelector tenantJWSKeySelector() {
        return new TenantJWSKeySelector();
    }

    /**
     * Provides a JWT processor configured with the tenant-aware JWS key selector.
     *
     * @return the configured JWT processor
     */
    @Bean
    public JWTProcessor<SecurityContext> jwtProcessor() {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(tenantJWSKeySelector());
        return jwtProcessor;
    }


    /**
     * Provides the validator that checks a JWT's issuer against the expected tenant issuer.
     *
     * @return the tenant JWT issuer validator
     */
    @Bean
    public TenantJwtIssuerValidator tenantJwtIssuerValidator() {
        return new TenantJwtIssuerValidator();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(this::allowAccess).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder(jwtProcessor(), tenantJwtIssuerValidator()))))
                .headers(header -> header
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                        .permissionsPolicyHeader(permissions -> permissions.policy("camera=(self), microphone=(self), geolocation=(self)"))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src *; object-src *"))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(63072000))
                        .addHeaderWriter((request, response) -> response.setHeader("Strict-Transport-Security", "max-age=63072000 ; includeSubDomains")));
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    private boolean allowAccess(HttpServletRequest request) {

        // Browsable JavaDoc is public, served by the app itself at /javadoc/** (see ResourceConfigs).
        if (request.getServletPath().startsWith("/javadoc")) {
            return true;
        }

        // Asset/QR/floor images are public static files served by the app at /images/** (see
        // ResourceConfigs), so an <img> tag can load them directly without an auth token.
        if (request.getServletPath().startsWith("/images")) {
            return true;
        }

        // QR-code manual test harness (developer/QA only), served at /qr-test/** (see ResourceConfigs).
        if (request.getServletPath().startsWith("/qr-test")) {
            return true;
        }

        String enableAuthHeader = request.getHeader("X-Enable-Auth");
        log.debug("{}", "X-Enable-Auth " + enableAuthHeader);
        if ((!request.getServletPath().contains("/ws")) && "true".equalsIgnoreCase(enableAuthHeader)) {
            log.debug("{}", "L4 Proxy request...");
            return false;
        }

        String clientIp = request.getRemoteAddr();
        log.debug("{}", "The clientIP is " + clientIp);
        boolean isInRange = false;
        try {
            String range = utils.getScleraBridgeSubnet();
            if (range != null) {
                InetAddress inetAddress = InetAddress.getByName(clientIp);
                if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                    SubnetUtils subnetUtils = new SubnetUtils(range);
                    isInRange = subnetUtils.getInfo().isInRange(clientIp);
                }
            }
        } catch (UnknownHostException e) {
            log.debug("{}", e);
        }


        List<String> ipAddresses = null;
        String remoteAddr = null;
        try {
            ipAddresses = this.getLocalIPAddresses();
            remoteAddr = request.getRemoteAddr();
        } catch (Exception e) {
            log.debug("{}", e);
        }

        return clientIp.equals("0:0:0:0:0:0:0:1") ||
                (ipAddresses != null && ipAddresses.contains(remoteAddr)) ||
                isInRange || clientIp.equals("127.0.0.1") ||
                request.getServletPath().contains("/ws");


    }

    /**
     * Collects the host addresses of all active, non-virtual network interfaces, excluding
     * link-local addresses.
     *
     * @return the list of local IP addresses
     */
    public static List<String> getLocalIPAddresses() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isVirtual()) continue;
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress currentAddress = addresses.nextElement();
                    if (currentAddress.isLinkLocalAddress()) continue;
                    ipAddresses.add(currentAddress.getHostAddress());
                }
            }
        } catch (SocketException e) {
            log.debug("{}", "Error getting local IP addresses: " + e.getMessage());
        }
        return ipAddresses;
    }


}



