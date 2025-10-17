package com.devhour.domain.exception;

/**
 * JIRA認証例外クラス
 * 
 * JIRA APIの認証エラー（HTTP 401, 403）が発生した場合に発生する例外。
 * この例外が発生した場合はリトライせず、即座に管理者に通知する。
 * 
 * REQ-8.4に対応: 認証エラー時の即座の通知処理
 */
public class JiraAuthenticationException extends JiraSyncException {
    
    private final int statusCode;
    
    /**
     * 認証例外を作成
     * 
     * @param message エラーメッセージ
     * @param statusCode HTTPステータスコード（401または403）
     */
    public JiraAuthenticationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * 認証例外を作成（原因例外付き）
     * 
     * @param message エラーメッセージ
     * @param cause 原因例外
     * @param statusCode HTTPステータスコード（401または403）
     */
    public JiraAuthenticationException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    /**
     * HTTPステータスコードを取得
     * 
     * @return HTTPステータスコード
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * 認証エラーかどうかを判定
     * 
     * @return 401認証エラーの場合true
     */
    public boolean isUnauthorized() {
        return statusCode == 401;
    }
    
    /**
     * 権限エラーかどうかを判定
     * 
     * @return 403権限エラーの場合true
     */
    public boolean isForbidden() {
        return statusCode == 403;
    }
}