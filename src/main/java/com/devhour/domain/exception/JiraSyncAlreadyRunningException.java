package com.devhour.domain.exception;

/**
 * JIRA同期既実行中例外
 * 
 * 同期処理が既に実行中の状態で新しい同期処理を開始しようとした場合にスローされる例外
 * システムは同時に複数の同期処理を実行できないため、この例外により重複実行を防止する
 */
public class JiraSyncAlreadyRunningException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 実行中の同期ID
     */
    private final String runningSyncId;
    
    /**
     * エラーメッセージを指定してJira同期既実行中例外を作成
     * 
     * @param message エラーメッセージ
     */
    public JiraSyncAlreadyRunningException(String message) {
        super(message);
        this.runningSyncId = null;
    }
    
    /**
     * エラーメッセージと実行中の同期IDを指定してJira同期既実行中例外を作成
     * 
     * @param message エラーメッセージ
     * @param runningSyncId 実行中の同期ID
     */
    public JiraSyncAlreadyRunningException(String message, String runningSyncId) {
        super(message);
        this.runningSyncId = runningSyncId;
    }
    
    /**
     * エラーメッセージと原因例外を指定してJira同期既実行中例外を作成
     * 
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public JiraSyncAlreadyRunningException(String message, Throwable cause) {
        super(message, cause);
        this.runningSyncId = null;
    }
    
    /**
     * エラーメッセージ、実行中の同期ID、原因例外を指定してJira同期既実行中例外を作成
     * 
     * @param message エラーメッセージ
     * @param runningSyncId 実行中の同期ID
     * @param cause 原因例外
     */
    public JiraSyncAlreadyRunningException(String message, String runningSyncId, Throwable cause) {
        super(message, cause);
        this.runningSyncId = runningSyncId;
    }
    
    /**
     * 実行中の同期IDを取得
     * 
     * @return 実行中の同期ID（設定されていない場合はnull）
     */
    public String getRunningSyncId() {
        return runningSyncId;
    }
}