package com.devhour.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Okta Security Utilities
 * 
 * Provides utility methods for extracting user information from JWT tokens
 */
@Component
public class OktaSecurityUtils {

    /**
     * Get the current Okta User ID from the security context
     * Note: In Okta, the 'sub' claim contains the user's email address,
     * which we use as the unique identifier (stored in okta_user_id column)
     * 
     * @return Optional containing the Okta User ID (email from sub claim) if authenticated, empty otherwise
     */
    public Optional<String> getCurrentOktaUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return Optional.ofNullable(jwt.getSubject()); // 'sub' claim contains email in Okta
        }
        
        return Optional.empty();
    }

    /**
     * Get the current user's email from the security context
     * 
     * @return Optional containing the email if authenticated, empty otherwise
     */
    public Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return Optional.ofNullable(jwt.getClaimAsString("email"));
        }
        
        return Optional.empty();
    }

    /**
     * Get the current JWT token from the security context
     * 
     * @return Optional containing the JWT if authenticated, empty otherwise
     */
    public Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof Jwt) {
            return Optional.of((Jwt) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }

    /**
     * Extract Okta User ID from a JWT token
     * Note: In Okta, 'sub' claim contains the user's email address
     * 
     * @param jwt The JWT token
     * @return The Okta User ID (email from 'sub' claim)
     */
    public String extractOktaUserId(Jwt jwt) {
        return jwt.getSubject(); // 'sub' claim contains email in Okta
    }

    /**
     * Extract email from a JWT token
     * 
     * @param jwt The JWT token
     * @return The user's email address
     */
    public String extractEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }

    /**
     * Extract user's full name from a JWT token
     * 
     * @param jwt The JWT token
     * @return The user's full name
     */
    public String extractFullName(Jwt jwt) {
        return jwt.getClaimAsString("name");
    }

    /**
     * Check if the current user has a specific scope
     * 
     * @param scope The scope to check (without SCOPE_ prefix)
     * @return true if the user has the scope, false otherwise
     */
    public boolean hasScope(String scope) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SCOPE_" + scope));
        }
        
        return false;
    }

    /**
     * Get the current database user ID (if the Okta user has been synced)
     * This requires the OktaUserSyncService to have been executed
     * 
     * @param userRepository The user repository to query
     * @return Optional containing the database user ID if found, empty otherwise
     */
    public Optional<String> getCurrentDatabaseUserId(com.devhour.domain.repository.UserRepository userRepository) {
        Optional<String> oktaUserId = getCurrentOktaUserId();
        
        if (oktaUserId.isPresent()) {
            return userRepository.findByOktaUserId(oktaUserId.get())
                .map(com.devhour.domain.model.entity.User::getId);
        }
        
        return Optional.empty();
    }
}