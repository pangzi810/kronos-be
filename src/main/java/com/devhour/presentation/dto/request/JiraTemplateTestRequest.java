package com.devhour.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * テンプレートテストリクエストDTO
 * 
 * レスポンステンプレートのテスト実行APIのリクエストボディを表現するデータ転送オブジェクト
 * サンプルJSONデータとテンプレートの組み合わせによる変換テストを実行する
 * 
 * 責務:
 * - テンプレートテスト用のサンプルデータ受け渡し
 * - JSON形式のテストデータバリデーション
 * - テンプレート変換テスト機能のリクエスト形式統一
 */
public record JiraTemplateTestRequest(
    
    /**
     * テストデータ（JSON文字列）
     * 
     * Velocityテンプレートに適用するサンプルJIRAレスポンスデータ
     * 有効なJSON形式である必要がある
     * JIRAのプロジェクト情報やイシュー情報を模擬したデータを想定
     */
    @NotBlank(message = "テストデータは必須です")
    String testData
    
) {
    
    /**
     * テストリクエストを作成
     * 
     * @param testData テストデータ（JSON文字列）
     * @return TemplateTestRequestインスタンス
     */
    public static JiraTemplateTestRequest of(String testData) {
        return new JiraTemplateTestRequest(testData);
    }
}