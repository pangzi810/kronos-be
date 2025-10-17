package com.devhour.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * LogBasedAdminNotificationServiceのユニットテスト
 * 
 * 管理者通知サービスの実装をテスト
 * 現在はログベースの実装のため、例外が発生しないことを確認
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogBasedAdminNotificationService")
class LogBasedAdminNotificationServiceTest {
    
    private LogBasedAdminNotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        notificationService = new LogBasedAdminNotificationService();
    }
    
    @Test
    @DisplayName("notifyAuthenticationError - 認証エラー通知が正常に実行される")
    void notifyAuthenticationError_ExecutedSuccessfully() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificationService.notifyAuthenticationError(
                "Authentication failed", 
                401, 
                "testQuery"
            );
        });
    }
    
    @Test
    @DisplayName("notifyRetryExhausted - リトライ失敗通知が正常に実行される")
    void notifyRetryExhausted_ExecutedSuccessfully() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificationService.notifyRetryExhausted(
                "All retries failed", 
                "testQuery", 
                3, 
                new RuntimeException("Last exception")
            );
        });
    }
    
    @Test
    @DisplayName("notifyRetryExhausted - 例外がnullでも正常に実行される")
    void notifyRetryExhausted_WithNullException_ExecutedSuccessfully() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            notificationService.notifyRetryExhausted(
                "All retries failed", 
                "testQuery", 
                3, 
                null
            );
        });
    }
    
}