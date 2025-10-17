package com.devhour.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JiraRateLimitExceptionのユニットテスト
 * 
 * レート制限例外の動作をテスト
 */
@DisplayName("JiraRateLimitException")
class JiraRateLimitExceptionTest {
    
    @Test
    @DisplayName("コンストラクタ - メッセージとリトライ時間で正常に作成")
    void constructor_WithMessageAndRetryAfter_CreatedSuccessfully() {
        // Arrange
        String message = "Rate limit exceeded";
        long retryAfterSeconds = 60L;
        
        // Act
        JiraRateLimitException exception = new JiraRateLimitException(message, retryAfterSeconds);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(retryAfterSeconds, exception.getRetryAfterSeconds());
        assertEquals(retryAfterSeconds * 1000L, exception.getRetryAfterMillis());
    }
    
    @Test
    @DisplayName("コンストラクタ - メッセージ、原因、リトライ時間で正常に作成")
    void constructor_WithMessageCauseAndRetryAfter_CreatedSuccessfully() {
        // Arrange
        String message = "Rate limit exceeded";
        RuntimeException cause = new RuntimeException("HTTP 429");
        long retryAfterSeconds = 120L;
        
        // Act
        JiraRateLimitException exception = new JiraRateLimitException(message, cause, retryAfterSeconds);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(retryAfterSeconds, exception.getRetryAfterSeconds());
        assertEquals(retryAfterSeconds * 1000L, exception.getRetryAfterMillis());
    }
    
    @Test
    @DisplayName("getRetryAfterMillis - 秒からミリ秒への変換が正しい")
    void getRetryAfterMillis_ConversionCorrect() {
        // Arrange
        long retryAfterSeconds = 30L;
        JiraRateLimitException exception = new JiraRateLimitException("Rate limit", retryAfterSeconds);
        
        // Act
        long retryAfterMillis = exception.getRetryAfterMillis();
        
        // Assert
        assertEquals(30_000L, retryAfterMillis);
    }
    
    @Test
    @DisplayName("JiraSyncExceptionの継承 - 親クラスのメソッドが使用可能")
    void inheritance_ParentClassMethodsAvailable() {
        // Arrange
        JiraRateLimitException exception = new JiraRateLimitException("Rate limit", 60L);
        
        // Act & Assert
        assertNotNull(exception.getMessage());
        // JiraSyncExceptionを継承しているため、RuntimeExceptionのメソッドが使用可能
        assertNotNull(exception.getClass().getSuperclass());
    }
}