package com.devhour.presentation.dto.response;

/**
 * テンプレートテスト結果DTO
 * 
 * レスポンステンプレートのテスト実行結果を表現するデータ転送オブジェクト
 * Velocityテンプレートにサンプルデータを適用した変換結果を含む
 * 
 * 責務:
 * - テンプレート変換テストの実行結果構造化
 * - 成功・失敗状態の表現
 * - 変換結果またはエラー情報の提供
 * - テンプレート検証機能のレスポンス形式統一
 */
public record TemplateTestResponse(
    
    /**
     * テスト実行成功フラグ
     * 
     * true: テンプレート変換が正常に実行された
     * false: 変換エラーまたは構文エラーが発生した
     */
    boolean success,
    
    /**
     * 変換結果文字列
     * 
     * テスト実行が成功した場合のVelocityテンプレート変換結果
     * 失敗した場合はnull
     */
    String result,
    
    /**
     * エラーメッセージ
     * 
     * テスト実行が失敗した場合のエラー詳細情報
     * 成功した場合はnull
     * Velocity構文エラーや変換エラーの詳細を含む
     */
    String errorMessage,
    
    /**
     * 実行時間（ミリ秒）
     * 
     * テンプレート変換処理にかかった時間
     * パフォーマンス監視とテンプレート最適化の参考情報
     */
    long executionTimeMs
    
) {
    
    /**
     * 成功時のテスト結果を作成
     * 
     * @param result 変換結果文字列
     * @param executionTimeMs 実行時間（ミリ秒）
     * @return 成功TemplateTestResultインスタンス
     */
    public static TemplateTestResponse success(String result, long executionTimeMs) {
        return new TemplateTestResponse(true, result, null, executionTimeMs);
    }
    
    /**
     * 失敗時のテスト結果を作成
     * 
     * @param errorMessage エラーメッセージ
     * @param executionTimeMs 実行時間（ミリ秒）
     * @return 失敗TemplateTestResultインスタンス
     */
    public static TemplateTestResponse failure(String errorMessage, long executionTimeMs) {
        return new TemplateTestResponse(false, null, errorMessage, executionTimeMs);
    }
    
    /**
     * 失敗時のテスト結果を作成（実行時間なし）
     * 
     * @param errorMessage エラーメッセージ
     * @return 失敗TemplateTestResultインスタンス
     */
    public static TemplateTestResponse failure(String errorMessage) {
        return new TemplateTestResponse(false, null, errorMessage, 0L);
    }
}