package com.devhour.infrastructure.security;

import com.devhour.domain.exception.UnauthorizedException;
import com.devhour.domain.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Security utility methods for OAuth2/JWT authentication
 * 
 * 更新版: OktaAuthenticationTokenをサポートし、内部ユーザー情報へのアクセスを提供
 */
public class SecurityUtils {
    
    /**
     * Get the current authenticated internal user
     * @return Optional containing the internal User entity if available, empty otherwise
     */
    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OktaAuthenticationToken) {
            OktaAuthenticationToken oktaAuth = (OktaAuthenticationToken) authentication;
            return Optional.ofNullable(oktaAuth.getInternalUser());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated user's internal ID
     * @return Optional containing the internal user ID if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OktaAuthenticationToken) {
            OktaAuthenticationToken oktaAuth = (OktaAuthenticationToken) authentication;
            return Optional.of(oktaAuth.getInternalUserId());
        } else if (authentication instanceof JwtAuthenticationToken) {
            // Fallback for standard JWT token (when OktaAuthenticationToken is not used)
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getClaimAsString("sub"));
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated user's Okta ID (sub claim)
     * @return Optional containing the Okta user ID if authenticated, empty otherwise
     */
    public static Optional<String> getOktaUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OktaAuthenticationToken) {
            OktaAuthenticationToken oktaAuth = (OktaAuthenticationToken) authentication;
            return Optional.of(oktaAuth.getOktaUserId());
        } else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getClaimAsString("sub"));
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated user's username
     * @return Optional containing the username if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OktaAuthenticationToken) {
            OktaAuthenticationToken oktaAuth = (OktaAuthenticationToken) authentication;
            User internalUser = oktaAuth.getInternalUser();
            if (internalUser != null) {
                return Optional.of(internalUser.getUsername());
            }
            // Fallback to JWT claims if internal user not available
            Jwt jwt = oktaAuth.getToken();
            String username = jwt.getClaimAsString("preferred_username");
            if (username == null) {
                username = jwt.getClaimAsString("email");
            }
            return Optional.ofNullable(username);
        } else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("preferred_username");
            if (username == null) {
                username = jwt.getClaimAsString("email");
            }
            return Optional.ofNullable(username);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated user's email
     * @return Optional containing the email if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OktaAuthenticationToken) {
            OktaAuthenticationToken oktaAuth = (OktaAuthenticationToken) authentication;
            return Optional.ofNullable(oktaAuth.getEmail());
        } else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getClaimAsString("email"));
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the current authenticated user's full name
     * @return Optional containing the full name if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUserFullName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof OktaAuthenticationToken) {
            OktaAuthenticationToken oktaAuth = (OktaAuthenticationToken) authentication;
            return Optional.ofNullable(oktaAuth.getFullName());
        } else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();
            return Optional.ofNullable(jwt.getClaimAsString("name"));
        }
        
        return Optional.empty();
    }
    
    /**
     * Check if the current user is authenticated
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * Get the current authenticated user's ID, throwing UnauthorizedException if not found
     * @return the user ID (never null)
     * @throws UnauthorizedException if no authenticated user ID is found
     */
    public static String requireCurrentUserId() {
        return getCurrentUserId()
            .orElseThrow(UnauthorizedException::userIdNotFound);
    }
    
    /**
     * Get the current authenticated user's username, throwing UnauthorizedException if not found
     * @return the username (never null)
     * @throws UnauthorizedException if no authenticated username is found
     */
    public static String requireCurrentUsername() {
        return getCurrentUsername()
            .orElseThrow(() -> new UnauthorizedException("Current username not found - authentication required"));
    }
}