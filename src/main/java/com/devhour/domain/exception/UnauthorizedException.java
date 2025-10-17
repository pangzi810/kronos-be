package com.devhour.domain.exception;

/**
 * 認証エラー例外
 * 
 * ユーザー認証に関する問題が発生した場合にスローされる例外
 * SecurityUtilsからユーザーIDが取得できない場合などに使用される
 */
public class UnauthorizedException extends DomainException {
    
    /**
     * メッセージ付き認証エラー例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     */
    public UnauthorizedException(String message) {
        super(message);
    }
    
    /**
     * メッセージと原因付き認証エラー例外のコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * ユーザーID取得失敗用のファクトリメソッド
     * 
     * @return UnauthorizedException
     */
    public static UnauthorizedException userIdNotFound() {
        return new UnauthorizedException("Current user ID not found - authentication required");
    }
    
    /**
     * 認証が必要な操作の実行試行用のファクトリメソッド
     * 
     * @param operation 操作名
     * @return UnauthorizedException
     */
    public static UnauthorizedException authenticationRequired(String operation) {
        return new UnauthorizedException("Authentication required for operation: " + operation);
    }
}