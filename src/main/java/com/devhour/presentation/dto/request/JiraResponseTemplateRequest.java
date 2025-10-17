package com.devhour.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * レスポンステンプレートリクエストDTO
 * 
 * レスポンステンプレート作成・更新APIのリクエストボディを表現するデータ転送オブジェクト
 * Velocityテンプレートとその設定情報を含む
 * 
 * 責務:
 * - レスポンステンプレート作成・更新リクエストデータの構造化
 * - バリデーション注釈によるリクエストデータ検証
 * - テンプレート名、Velocityテンプレート、説明の受け渡し
 * 
 * バリデーション要件:
 * - テンプレート名: 必須、最大100文字
 * - Velocityテンプレート: 必須、最大10000文字
 * - テンプレート説明: 任意、最大500文字
 */
public record JiraResponseTemplateRequest(
    
    /**
     * テンプレート名
     * 
     * システム内で一意である必要がある
     * JQLクエリ設定時のテンプレート選択で使用される
     */
    @NotBlank(message = "テンプレート名は必須です")
    @Size(max = 100, message = "テンプレート名は100文字以内で入力してください")
    String templateName,
    
    /**
     * Velocityテンプレート
     * 
     * JIRA APIレスポンスを共通フォーマットJSONに変換するためのテンプレート
     * Apache Velocity構文に従って記述される
     */
    @NotBlank(message = "Velocityテンプレートは必須です")
    @Size(max = 10000, message = "Velocityテンプレートは10000文字以内で入力してください")
    String velocityTemplate,
    
    /**
     * テンプレート説明
     * 
     * テンプレートの用途や使用方法を説明する任意フィールド
     * 管理画面でのテンプレート選択時に参考情報として表示される
     */
    @Size(max = 500, message = "テンプレート説明は500文字以内で入力してください")
    String templateDescription
    
) {
    
    /**
     * 簡略版リクエスト作成（説明なし）
     * 
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @return ResponseTemplateRequestインスタンス
     */
    public static JiraResponseTemplateRequest of(String templateName, String velocityTemplate) {
        return new JiraResponseTemplateRequest(templateName, velocityTemplate, null);
    }
    
    /**
     * 完全版リクエスト作成
     * 
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @param templateDescription テンプレート説明
     * @return ResponseTemplateRequestインスタンス
     */
    public static JiraResponseTemplateRequest of(String templateName, String velocityTemplate, String templateDescription) {
        return new JiraResponseTemplateRequest(templateName, velocityTemplate, templateDescription);
    }
}