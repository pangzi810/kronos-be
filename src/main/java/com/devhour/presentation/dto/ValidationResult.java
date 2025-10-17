package com.devhour.presentation.dto;

/**
 * JQL検証結果レスポンス
 * 
 * JQLクエリの構文検証結果を表現するDTO
 * バリデーション結果の成功/失敗、マッチング数、エラーメッセージを含む
 * 
 * 使用箇所:
 * - POST /api/jira/queries/{id}/validate (JQL検証エンドポイント)
 * 
 * 要件対応:
 * - REQ-2.2: JQL構文検証機能
 * - REQ-2.3: バリデーション結果表示機能
 */
public class ValidationResult {
    
    private boolean valid;
    private int matchingCount;
    private String errorMessage;
    
    /**
     * デフォルトコンストラクタ
     */
    public ValidationResult() {
    }
    
    /**
     * プライベートコンストラクタ
     * ファクトリーメソッドからのみ使用
     * 
     * @param valid 有効性フラグ
     * @param matchingCount マッチング数
     * @param errorMessage エラーメッセージ
     */
    private ValidationResult(boolean valid, int matchingCount, String errorMessage) {
        this.valid = valid;
        this.matchingCount = matchingCount;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 有効なJQL検証結果を作成
     * 
     * @param matchingCount マッチしたイシュー数
     * @return 有効な検証結果
     */
    public static ValidationResult valid(int matchingCount) {
        return new ValidationResult(true, matchingCount, null);
    }
    
    /**
     * 無効なJQL検証結果を作成
     * 
     * @param errorMessage エラーメッセージ
     * @return 無効な検証結果
     */
    public static ValidationResult invalid(String errorMessage) {
        return new ValidationResult(false, 0, errorMessage);
    }
    
    /**
     * JQLクエリが有効かどうかを判定
     * 
     * @return 有効な場合true
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * 有効性フラグを設定
     * 
     * @param valid 有効性フラグ
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /**
     * マッチするイシュー数を取得
     * 
     * @return マッチしたイシュー数（無効な場合は0）
     */
    public int getMatchingCount() {
        return matchingCount;
    }
    
    /**
     * マッチング数を設定
     * 
     * @param matchingCount マッチング数
     */
    public void setMatchingCount(int matchingCount) {
        this.matchingCount = matchingCount;
    }
    
    /**
     * エラーメッセージを取得
     * 
     * @return エラーメッセージ（有効な場合はnull）
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * エラーメッセージを設定
     * 
     * @param errorMessage エラーメッセージ
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * 文字列表現
     * 
     * @return ValidationResultの文字列表現
     */
    @Override
    public String toString() {
        return String.format("ValidationResult{valid=%s, matchingCount=%d, errorMessage='%s'}", 
                           valid, matchingCount, errorMessage);
    }
}