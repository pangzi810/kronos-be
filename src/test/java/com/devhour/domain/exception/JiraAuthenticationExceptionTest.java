package com.devhour.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JiraAuthenticationExceptionのユニットテスト
 * 
 * 認証例外の動作をテスト
 */
@DisplayName("JiraAuthenticationException")
class JiraAuthenticationExceptionTest {
    
    @Test
    @DisplayName("コンストラクタ - メッセージとステータスコードで正常に作成")
    void constructor_WithMessageAndStatusCode_CreatedSuccessfully() {
        // Arrange
        String message = "Authentication failed";
        int statusCode = 401;
        
        // Act
        JiraAuthenticationException exception = new JiraAuthenticationException(message, statusCode);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(statusCode, exception.getStatusCode());
    }
    
    @Test
    @DisplayName("コンストラクタ - メッセージ、原因、ステータスコードで正常に作成")
    void constructor_WithMessageCauseAndStatusCode_CreatedSuccessfully() {
        // Arrange
        String message = "Authentication failed";
        RuntimeException cause = new RuntimeException("HTTP error");
        int statusCode = 403;
        
        // Act
        JiraAuthenticationException exception = new JiraAuthenticationException(message, cause, statusCode);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(statusCode, exception.getStatusCode());
    }
    
    @Test
    @DisplayName("isUnauthorized - 401ステータスコードの場合true")
    void isUnauthorized_Status401_ReturnsTrue() {
        // Arrange
        JiraAuthenticationException exception = new JiraAuthenticationException("Unauthorized", 401);
        
        // Act & Assert
        assertTrue(exception.isUnauthorized());
        assertFalse(exception.isForbidden());
    }
    
    @Test
    @DisplayName("isForbidden - 403ステータスコードの場合true")
    void isForbidden_Status403_ReturnsTrue() {
        // Arrange
        JiraAuthenticationException exception = new JiraAuthenticationException("Forbidden", 403);
        
        // Act & Assert
        assertTrue(exception.isForbidden());
        assertFalse(exception.isUnauthorized());
    }
    
    @Test
    @DisplayName("isUnauthorized/isForbidden - その他のステータスコードの場合false")
    void isUnauthorized_OtherStatusCodes_ReturnsFalse() {
        // Arrange
        JiraAuthenticationException exception = new JiraAuthenticationException("Server error", 500);
        
        // Act & Assert
        assertFalse(exception.isUnauthorized());
        assertFalse(exception.isForbidden());
    }
    
    @Test
    @DisplayName("JiraSyncExceptionの継承 - 親クラスのメソッドが使用可能")
    void inheritance_ParentClassMethodsAvailable() {
        // Arrange
        JiraAuthenticationException exception = new JiraAuthenticationException("Auth failed", 401);
        
        // Act & Assert
        assertNotNull(exception.getMessage());
        // JiraSyncExceptionを継承しているため、RuntimeExceptionのメソッドが使用可能
        assertNotNull(exception.getClass().getSuperclass());
    }
}