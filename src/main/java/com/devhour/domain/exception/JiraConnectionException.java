package com.devhour.domain.exception;

/**
 * JIRA接続例外
 * 
 * JIRA APIへの接続に関連するエラーが発生した場合にスローされる例外
 * ネットワークエラー、タイムアウト、接続拒否などの接続関連の問題を表す
 */
public class JiraConnectionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * エラーメッセージを指定してJira接続例外を作成
     * 
     * @param message エラーメッセージ
     */
    public JiraConnectionException(String message) {
        super(message);
    }
    
    /**
     * エラーメッセージと原因例外を指定してJira接続例外を作成
     * 
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public JiraConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 原因例外のみを指定してJira接続例外を作成
     * 
     * @param cause 原因例外
     */
    public JiraConnectionException(Throwable cause) {
        super(cause);
    }
}