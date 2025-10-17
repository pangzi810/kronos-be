package com.devhour.domain.exception;

/**
 * JIRAレート制限例外クラス
 * 
 * JIRA APIのレート制限（HTTP 429）に達した場合に発生する例外。
 * APIレスポンスヘッダーからの待機時間情報を保持する。
 * 
 * REQ-8.3に対応: レート制限時の適応的リトライ処理
 */
public class JiraRateLimitException extends JiraSyncException {
    
    private final long retryAfterSeconds;
    
    /**
     * レート制限例外を作成
     * 
     * @param message エラーメッセージ
     * @param retryAfterSeconds APIが指定する待機時間（秒）
     */
    public JiraRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    /**
     * レート制限例外を作成（原因例外付き）
     * 
     * @param message エラーメッセージ
     * @param cause 原因例外
     * @param retryAfterSeconds APIが指定する待機時間（秒）
     */
    public JiraRateLimitException(String message, Throwable cause, long retryAfterSeconds) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    /**
     * APIが指定する待機時間を取得
     * 
     * @return 待機時間（秒）
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
    
    /**
     * 待機時間をミリ秒で取得
     * 
     * @return 待機時間（ミリ秒）
     */
    public long getRetryAfterMillis() {
        return retryAfterSeconds * 1000L;
    }
}