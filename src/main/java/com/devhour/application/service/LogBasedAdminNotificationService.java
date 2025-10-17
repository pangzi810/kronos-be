package com.devhour.application.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * ログベースの管理者通知サービス実装
 * 
 * 現在の実装ではログ出力による通知を行う。
 * 将来的にはメール送信、Slack通知などに拡張予定。
 * 
 * REQ-8.4, REQ-8.6に対応した通知機能の実装
 */
@Service
@Slf4j
public class LogBasedAdminNotificationService implements AdminNotificationService {
    
    private static final String ADMIN_NOTIFICATION_PREFIX = "[ADMIN_NOTIFICATION]";
    
    @Override
    public void notifyAuthenticationError(String errorMessage, int statusCode, String queryName) {
        log.error("{} JIRA認証エラー発生 - 即座の対応が必要です。" +
                 "QueryName: {}, StatusCode: {}, Error: {}", 
                 ADMIN_NOTIFICATION_PREFIX, queryName, statusCode, errorMessage);
        
        // TODO: 将来的にメール通知やSlack通知を実装
        // emailService.sendAuthenticationErrorNotification(errorMessage, statusCode, queryName);
    }
    
    @Override
    public void notifyRetryExhausted(String errorMessage, String queryName, int attemptCount, Throwable lastException) {
        log.error("{} JIRA同期の全リトライが失敗しました - 手動での確認が必要です。" +
                 "QueryName: {}, AttemptCount: {}, Error: {}, Exception: {}", 
                 ADMIN_NOTIFICATION_PREFIX, queryName, attemptCount, errorMessage, 
                 lastException != null ? lastException.getClass().getSimpleName() : "Unknown");
        
        if (lastException != null) {
            log.error("{} 最終例外詳細:", ADMIN_NOTIFICATION_PREFIX, lastException);
        }
        
        // TODO: 将来的にメール通知やSlack通知を実装
        // emailService.sendRetryExhaustedNotification(errorMessage, queryName, attemptCount, lastException);
    }
    
}