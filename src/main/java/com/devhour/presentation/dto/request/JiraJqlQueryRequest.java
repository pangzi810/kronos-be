package com.devhour.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JQLクエリ作成/更新リクエスト
 * 
 * JQLクエリの作成・更新時に使用するリクエストDTO
 * バリデーションアノテーションにより入力値の妥当性を保証する
 * 
 * 使用箇所:
 * - POST /api/jira/queries (新規作成)
 * - PUT /api/jira/queries/{id} (更新)
 * 
 * 要件対応:
 * - REQ-2.2: JQLクエリ登録機能
 * - REQ-2.3: JQL構文検証機能
 * - REQ-2.4: 実行優先度管理機能
 */
public class JiraJqlQueryRequest {
    
    @NotBlank(message = "クエリ名は必須です")
    @Size(max = 100, message = "クエリ名は100文字以内で入力してください")
    private String queryName;
    
    @NotBlank(message = "JQL式は必須です")
    @Size(max = 2000, message = "JQL式は2000文字以内で入力してください")
    private String jqlExpression;
    
    @NotBlank(message = "テンプレートIDは必須です")
    @Size(max = 36, message = "テンプレートIDは36文字以内で入力してください")
    private String templateId;
    
    @NotNull(message = "優先度は必須です")
    @Min(value = 0, message = "優先度は0以上である必要があります")
    private Integer priority;
    
    @NotNull(message = "アクティブ状態の指定は必須です")
    private Boolean isActive;
    
    /**
     * デフォルトコンストラクタ
     */
    public JiraJqlQueryRequest() {
        this.isActive = true; // デフォルトでアクティブ
    }
    
    /**
     * 全パラメータコンストラクタ（テスト用）
     * 
     * @param queryName JQLクエリ名
     * @param jqlExpression JQL式
     * @param templateId テンプレートID
     * @param priority 実行優先度
     * @param isActive アクティブ状態
     */
    public JiraJqlQueryRequest(String queryName, String jqlExpression, String templateId, 
                          Integer priority, Boolean isActive) {
        this.queryName = queryName;
        this.jqlExpression = jqlExpression;
        this.templateId = templateId;
        this.priority = priority;
        this.isActive = isActive != null ? isActive : true;
    }
    
    // ゲッター・セッター
    public String getQueryName() {
        return queryName;
    }
    
    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }
    
    public String getJqlExpression() {
        return jqlExpression;
    }
    
    public void setJqlExpression(String jqlExpression) {
        this.jqlExpression = jqlExpression;
    }
    
    public String getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}