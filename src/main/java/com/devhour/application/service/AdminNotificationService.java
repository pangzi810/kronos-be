package com.devhour.application.service;

/**
 * 管理者通知サービス
 * 
 * JIRA同期処理でのエラー発生時に管理者への通知を担当する。
 * 将来的にはメール通知、Slack通知などに拡張可能。
 * 
 * REQ-8.4, REQ-8.6に対応:
 * - 認証エラー時の即座の管理者通知
 * - 全リトライ失敗時の詳細エラー通知
 */
public interface AdminNotificationService {
    
    /**
     * JIRA認証エラーの管理者通知
     * 
     * 認証エラー（401/403）が発生した場合に即座に管理者に通知する。
     * REQ-8.4に対応。
     * 
     * @param errorMessage エラーメッセージ
     * @param statusCode HTTPステータスコード
     * @param queryName 実行中だったJQLクエリ名
     */
    void notifyAuthenticationError(String errorMessage, int statusCode, String queryName);
    
    /**
     * JIRA同期の全リトライ失敗通知
     * 
     * すべてのリトライが失敗した場合に詳細なエラー情報と共に
     * 管理者に通知する。REQ-8.6に対応。
     * 
     * @param errorMessage エラーメッセージ
     * @param queryName 実行中だったJQLクエリ名
     * @param attemptCount リトライ試行回数
     * @param lastException 最後に発生した例外
     */
    void notifyRetryExhausted(String errorMessage, String queryName, int attemptCount, Throwable lastException);
    
}