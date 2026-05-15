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

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


@Configuration
@EnableWebSecurity
@org.springframework.context.annotation.Profile("!docker")
public class WebSecurityConfig {


    @Autowired
    private Utils utils;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Bean
    public JwtDecoder jwtDecoder(JWTProcessor<SecurityContext> jwtProcessor, OAuth2TokenValidator<Jwt> jwtValidator) {
        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), jwtValidator);
        decoder.setJwtValidator(validator);
        return decoder;
    }


    @Bean
    public TenantJWSKeySelector tenantJWSKeySelector() {
        return new TenantJWSKeySelector();
    }

    @Bean
    public JWTProcessor<SecurityContext> jwtProcessor() {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(tenantJWSKeySelector());
        return jwtProcessor;
    }


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
                .authorizeRequests()
                .requestMatchers(this::allowAccess)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder(jwtProcessor(), tenantJwtIssuerValidator()))))
                .headers(header -> header
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                        .permissionsPolicy(permissions -> permissions.policy("camera=(self), microphone=(self), geolocation=(self)"))
                        .and()
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src *; object-src *"))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(63072000))
                        .addHeaderWriter((request, response) -> response.setHeader("Strict-Transport-Security", "max-age=63072000 ; includeSubDomains")));
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    private boolean allowAccess(HttpServletRequest request) {

        String enableAuthHeader = request.getHeader("X-Enable-Auth");
        System.out.println("X-Enable-Auth " + enableAuthHeader);
        if ((!request.getServletPath().contains("/ws")) && "true".equalsIgnoreCase(enableAuthHeader)) {
            System.out.println("L4 Proxy request...");
            return false;
        }

        String clientIp = request.getRemoteAddr();
        System.out.println("The clientIP is " + clientIp);
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
            System.out.println(e);
        }


        List<String> ipAddresses = null;
        String remoteAddr = null;
        try {
            ipAddresses = this.getLocalIPAddresses();
            remoteAddr = request.getRemoteAddr();
        } catch (Exception e) {
            System.out.println(e);
        }

        return clientIp.equals("0:0:0:0:0:0:0:1") ||
                (ipAddresses != null && ipAddresses.contains(remoteAddr)) ||
                isInRange || clientIp.equals("127.0.0.1") ||
                request.getServletPath().contains("/ws");


    }

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
            System.out.println("Error getting local IP addresses: " + e.getMessage());
        }
        return ipAddresses;
    }


}



