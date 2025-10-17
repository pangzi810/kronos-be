package com.devhour.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Okta User Identifier Extractor
 * 
 * Handles extraction of unique Okta user identifiers from JWT tokens.
 * Supports multiple strategies for finding the unique ID based on Okta configuration.
 */
@Component
public class OktaUserIdentifier {
    
    private static final Logger logger = LoggerFactory.getLogger(OktaUserIdentifier.class);
    
    /**
     * Extract the unique Okta User ID from JWT
     * 
     * Tries multiple strategies in order:
     * 1. 'uid' claim - Common in Okta custom claims
     * 2. 'sub' claim if it looks like Okta ID (starts with "00u")
     * 3. 'user_id' custom claim
     * 4. Falls back to email from 'sub' as identifier
     * 
     * @param jwt The JWT token
     * @return The unique identifier for the user
     */
    public String extractUniqueId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT token cannot be null");
        }
        
        // Strategy 1: Check for 'uid' claim (most common for Okta unique ID)
        String uid = jwt.getClaimAsString("uid");
        if (uid != null && !uid.trim().isEmpty()) {
            logger.debug("Found Okta ID in 'uid' claim: {}", uid);
            return uid;
        }
        
        // Strategy 2: Check if 'sub' contains Okta ID format (00uXXXXXX)
        String sub = jwt.getSubject();
        if (sub != null && isOktaIdFormat(sub)) {
            logger.debug("Found Okta ID in 'sub' claim: {}", sub);
            return sub;
        }
        
        // Strategy 3: Check for custom 'user_id' claim
        String userId = jwt.getClaimAsString("user_id");
        if (userId != null && !userId.trim().isEmpty()) {
            logger.debug("Found ID in 'user_id' claim: {}", userId);
            return userId;
        }
        
        // Strategy 4: Check for 'id' claim
        String id = jwt.getClaimAsString("id");
        if (id != null && !id.trim().isEmpty() && !id.equals("undefined")) {
            logger.debug("Found ID in 'id' claim: {}", id);
            return id;
        }
        
        // Strategy 5: Use email from sub as fallback identifier
        if (sub != null && sub.contains("@")) {
            logger.debug("Using email from 'sub' claim as identifier: {}", sub);
            return sub;
        }
        
        // Strategy 6: Use email claim as last resort
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.trim().isEmpty()) {
            logger.debug("Using 'email' claim as identifier: {}", email);
            return email;
        }
        
        throw new IllegalStateException("Could not extract unique identifier from JWT token");
    }
    
    /**
     * Extract the actual Okta User ID (if available)
     * This specifically looks for the Okta format ID (00uXXXXX)
     * 
     * @param jwt The JWT token
     * @return The Okta User ID if found, null otherwise
     */
    public String extractOktaUserId(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        
        // Check 'uid' claim first
        String uid = jwt.getClaimAsString("uid");
        if (uid != null && isOktaIdFormat(uid)) {
            return uid;
        }
        
        // Check 'sub' claim
        String sub = jwt.getSubject();
        if (sub != null && isOktaIdFormat(sub)) {
            return sub;
        }
        
        // Check custom claims that might contain Okta ID
        for (String claimName : jwt.getClaims().keySet()) {
            Object value = jwt.getClaim(claimName);
            if (value instanceof String) {
                String strValue = (String) value;
                if (isOktaIdFormat(strValue)) {
                    logger.debug("Found Okta ID in '{}' claim: {}", claimName, strValue);
                    return strValue;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Extract email from JWT
     * 
     * @param jwt The JWT token
     * @return The email address
     */
    public String extractEmail(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        
        // Check 'email' claim first
        String email = jwt.getClaimAsString("email");
        if (email != null && email.contains("@")) {
            return email;
        }
        
        // Check 'sub' claim if it contains email
        String sub = jwt.getSubject();
        if (sub != null && sub.contains("@")) {
            return sub;
        }
        
        // Check 'preferred_username' as fallback
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && preferredUsername.contains("@")) {
            return preferredUsername;
        }
        
        return null;
    }
    
    /**
     * Get user identifier info for debugging
     * 
     * @param jwt The JWT token
     * @return Debug information about identifiers
     */
    public UserIdentifierInfo getIdentifierInfo(Jwt jwt) {
        return new UserIdentifierInfo(
            extractUniqueId(jwt),
            extractOktaUserId(jwt),
            extractEmail(jwt),
            jwt.getSubject()
        );
    }
    
    /**
     * Check if a string matches Okta User ID format
     * Okta User IDs typically start with "00u" followed by alphanumeric characters
     * 
     * @param value The value to check
     * @return true if it matches Okta ID format
     */
    private boolean isOktaIdFormat(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        // Okta IDs typically: 00u + 15-20 alphanumeric characters
        return value.matches("^00[uo][a-zA-Z0-9]{15,20}$");
    }
    
    /**
     * User Identifier Information
     */
    public static class UserIdentifierInfo {
        private final String uniqueId;
        private final String oktaUserId;
        private final String email;
        private final String subClaim;
        
        public UserIdentifierInfo(String uniqueId, String oktaUserId, String email, String subClaim) {
            this.uniqueId = uniqueId;
            this.oktaUserId = oktaUserId;
            this.email = email;
            this.subClaim = subClaim;
        }
        
        public String getUniqueId() { return uniqueId; }
        public String getOktaUserId() { return oktaUserId; }
        public String getEmail() { return email; }
        public String getSubClaim() { return subClaim; }
        
        public boolean hasOktaUserId() {
            return oktaUserId != null && !oktaUserId.trim().isEmpty();
        }
        
        @Override
        public String toString() {
            return String.format("UserIdentifierInfo{uniqueId='%s', oktaUserId='%s', email='%s', sub='%s'}",
                uniqueId, oktaUserId, email, subClaim);
        }
    }
}