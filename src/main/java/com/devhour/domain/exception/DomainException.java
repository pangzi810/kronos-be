package com.devhour.domain.exception;

/**
 * ドメイン例外の基底クラス
 * 
 * ドメイン層で発生するビジネスルール違反やドメインロジック関連の例外の基底となるクラス
 * すべてのドメイン例外はこのクラスを継承する
 * 
 * 責務:
 * - ドメイン例外の統一的な基盤提供
 * - ドメイン例外とインフラ例外の明確な分離
 */
public abstract class DomainException extends RuntimeException {
    
    /**
     * メッセージ付きドメイン例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    protected DomainException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因付きドメイン例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}