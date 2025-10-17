package com.devhour.domain.exception;

/**
 * JIRA同期処理例外
 * 
 * JIRA統合機能において同期処理で発生するビジネスルール違反や
 * 同期処理関連のエラーを表す例外クラス
 * 
 * 責務:
 * - JIRA同期処理固有の例外情報管理
 * - コンフリクト解決処理におけるエラー情報提供
 * - 同期可能性チェック処理でのエラー通知
 */
public class JiraSyncException extends DomainException {
    
    /**
     * メッセージ付きJIRA同期例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public JiraSyncException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因付きJIRA同期例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public JiraSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}