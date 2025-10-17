package com.devhour.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified Test Security Configuration
 * 
 * This configuration provides a consistent security setup for all tests:
 * - Enables OAuth2 JWT authentication with mock decoder
 * - Supports scope-based authorization for @PreAuthorize annotations
 * - Provides test JWT tokens with all necessary scopes
 * - Takes precedence over production security configurations
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("test")
public class TestSecurityConfiguration {

    /**
     * Mock JWT decoder for testing - creates JWT tokens with default scopes
     * 
     * Note: This decoder is used as a fallback for tests that don't specify custom JWTs.
     * When tests use .with(jwt().jwt(jwt -> jwt.claim("scope", "custom"))), 
     * Spring Security Test bypasses this decoder and uses the test-specified JWT directly.
     * 
     * However, due to the current Spring Boot Test configuration, this decoder 
     * is being called even for test-specified JWTs, which is a known limitation.
     */
    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> {
            // Create a mock JWT with all necessary claims and scopes
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "none");
            headers.put("typ", "JWT");
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "test-user");
            claims.put("preferred_username", "testuser");
            claims.put("email", "test@example.com");
            
            claims.put("scope", "projects:create:own projects:read:assigned projects:read:managed projects:read:all " +
                              "projects:write:managed projects:write:all work-records:create work-records:read:own " +
                              "work-records:read:managed work-records:read:all work-records:write:own work-records:write:managed " +
                              "work-records:write:all work-categories:read work-categories:write users:read:all " +
                              "users:write:all jql-queries:read jql-queries:write " +
                              "system:manage:integration reports:read:all");
            claims.put("iat", Instant.now().getEpochSecond());
            claims.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());
            
            return Jwt.withTokenValue(token)
                .headers(h -> h.putAll(headers))
                .claims(c -> c.putAll(claims))
                .build();
        };
    }

    /**
     * JWT authentication converter for converting JWT to authentication with proper authorities
     */
    @Bean
    public JwtAuthenticationConverter testJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String scopes = jwt.getClaimAsString("scope");
            return scopes == null ? 
                java.util.Collections.emptyList() : 
                java.util.Arrays.stream(scopes.split(" "))
                    .map(scope -> new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(java.util.stream.Collectors.toList());
        });
        return converter;
    }

    /**
     * Test password encoder bean - same as production for compatibility
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Test security filter chain - enables JWT authentication with all scopes
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection for API testing
            .csrf(csrf -> csrf.disable())
            
            // Disable CORS (not needed in tests)
            .cors(cors -> cors.disable())
            
            // Set session management to stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure OAuth2 resource server for JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(testJwtDecoder())
                    .jwtAuthenticationConverter(testJwtAuthenticationConverter())
                )
            )
            
            // Allow all requests - authorization handled by method security
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            )
            
            // Disable security headers for H2 console (if needed)
            .headers(headers -> headers
                .frameOptions().disable()
                .httpStrictTransportSecurity().disable()
            );
        
        return http.build();
    }
}