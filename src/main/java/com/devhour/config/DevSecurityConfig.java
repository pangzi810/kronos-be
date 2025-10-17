package com.devhour.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Development Security Configuration
 * 
 * This configuration is used when Okta authentication is disabled
 * Allows all requests for local development
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
    name = "security.okta.enabled", 
    havingValue = "false", 
    matchIfMissing = false
)
@Profile("!test")
public class DevSecurityConfig {
    
    /**
     * PasswordEncoder Bean for user service
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Security filter chain for development - allows all requests
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS
            .cors(cors -> {})
            
            // Session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Permit all requests for development
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}