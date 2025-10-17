package com.devhour.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.devhour.config.JiraClientConfiguration.JiraApiException;

/**
 * RetryConfigurationのユニットテスト
 * 
 * Spring Retryの設定とリトライ可能な例外の判定ロジックをテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetryConfiguration")
class RetryConfigurationTest {
    
    private final RetryConfiguration retryConfiguration = new RetryConfiguration();
    
    @Test
    @DisplayName("jiraSyncRetryTemplate - 正常にBean作成")
    void jiraSyncRetryTemplate_CreateSuccessfully() {
        // Act
        RetryTemplate retryTemplate = retryConfiguration.jiraSyncRetryTemplate();
        
        // Assert
        assertNotNull(retryTemplate);
    }
    
    @Test
    @DisplayName("isRetryableException - ネットワーク関連エラーはリトライ可能")
    void isRetryableException_NetworkErrors_Retryable() {
        // Act & Assert
        assertTrue(RetryConfiguration.isRetryableException(new ResourceAccessException("Connection failed")));
        assertTrue(RetryConfiguration.isRetryableException(new ConnectException("Connection refused")));
        assertTrue(RetryConfiguration.isRetryableException(new SocketTimeoutException("Read timeout")));
        assertTrue(RetryConfiguration.isRetryableException(new TimeoutException("Operation timeout")));
        assertTrue(RetryConfiguration.isRetryableException(HttpServerErrorException.create(
            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null)));
    }
    
    @Test
    @DisplayName("isRetryableException - 認証エラーはリトライ不可")
    void isRetryableException_AuthenticationErrors_NotRetryable() {
        // Act & Assert
        assertFalse(RetryConfiguration.isRetryableException(new JiraApiException("Unauthorized", 401)));
        assertFalse(RetryConfiguration.isRetryableException(new JiraApiException("Forbidden", 403)));
    }
    
    @Test
    @DisplayName("isRetryableException - レート制限エラーはリトライ可能")
    void isRetryableException_RateLimitError_Retryable() {
        // Act & Assert
        assertTrue(RetryConfiguration.isRetryableException(new JiraApiException("Too Many Requests", 429)));
    }
    
    @Test
    @DisplayName("isRetryableException - クライアントエラーはリトライ不可")
    void isRetryableException_ClientErrors_NotRetryable() {
        // Act & Assert
        assertFalse(RetryConfiguration.isRetryableException(new JiraApiException("Bad Request", 400)));
        assertFalse(RetryConfiguration.isRetryableException(new JiraApiException("Not Found", 404)));
        assertFalse(RetryConfiguration.isRetryableException(new JiraApiException("Method Not Allowed", 405)));
    }
    
    @Test
    @DisplayName("isRetryableException - サーバーエラーはリトライ可能")
    void isRetryableException_ServerErrors_Retryable() {
        // サーバーエラー（5xx）はHttpServerErrorExceptionとして扱われる
        // JiraApiExceptionの場合、5xxレンジは特に処理されないため、デフォルトでリトライ不可
        // ただし、HttpServerErrorExceptionの場合はリトライ可能として扱われる
        
        // Act & Assert
        assertTrue(RetryConfiguration.isRetryableException(HttpServerErrorException.create(
            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null)));
        assertTrue(RetryConfiguration.isRetryableException(HttpServerErrorException.create(
            org.springframework.http.HttpStatus.BAD_GATEWAY, "Bad Gateway", null, null, null)));
    }
    
    @Test
    @DisplayName("isRetryableException - その他の例外はリトライ不可")
    void isRetryableException_OtherExceptions_NotRetryable() {
        // Act & Assert
        assertFalse(RetryConfiguration.isRetryableException(new RuntimeException("Generic error")));
        assertFalse(RetryConfiguration.isRetryableException(new IllegalArgumentException("Invalid argument")));
        assertFalse(RetryConfiguration.isRetryableException(new NullPointerException()));
    }
}