package com.devhour.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * JQLクエリエンティティ
 * 
 * JIRA同期機能で使用するJQLクエリとその実行設定を管理するエンティティ
 * 
 * 責務:
 * - JQLクエリ情報の管理
 * - 実行優先度の制御
 * - アクティブ状態の管理
 * - テンプレート関連付けの管理
 */
public class JiraJqlQuery {
    
    private String id;
    private String queryName;
    private String jqlExpression;
    private String templateId;
    private Boolean isActive;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    private JiraJqlQuery() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }

    /**
     * プライベートコンストラクタ（新規作成用）
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private JiraJqlQuery(String id, String queryName, String jqlExpression, String templateId, 
                    Integer priority, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.queryName = queryName;
        this.jqlExpression = jqlExpression;
        this.templateId = templateId;
        this.isActive = true;
        this.priority = priority;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.updatedBy = null;
    }
    
    /**
     * 既存JQLクエリの復元用コンストラクタ
     * リポジトリからの読み込み時に使用
     */
    private JiraJqlQuery(String id, String queryName, String jqlExpression, String templateId,
                    Boolean isActive, Integer priority, LocalDateTime createdAt, LocalDateTime updatedAt,
                    String createdBy, String updatedBy) {
        this.id = id;
        this.queryName = queryName;
        this.jqlExpression = jqlExpression;
        this.templateId = templateId;
        this.isActive = isActive;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
    
    /**
     * 新しいJQLクエリを作成するファクトリーメソッド
     * 
     * @param queryName クエリ名
     * @param jqlExpression JQL式
     * @param templateId テンプレートID
     * @param priority 実行優先度
     * @param createdBy 作成者ID
     * @return 新しいJqlQueryエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static JiraJqlQuery createNew(String queryName, String jqlExpression, String templateId,
                                   Integer priority, String createdBy) {
        validateCreateParameters(queryName, jqlExpression, templateId, priority, createdBy);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new JiraJqlQuery(id, queryName.trim(), jqlExpression.trim(), templateId.trim(),
                          priority, createdBy.trim(), now);
    }
    
    /**
     * 既存JQLクエリを復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     * 
     * @param id JQLクエリID
     * @param queryName クエリ名
     * @param jqlExpression JQL式
     * @param templateId テンプレートID
     * @param isActive アクティブフラグ
     * @param priority 実行優先度
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @param createdBy 作成者ID
     * @param updatedBy 更新者ID
     * @return 復元されたJqlQueryエンティティ
     */
    public static JiraJqlQuery restore(String id, String queryName, String jqlExpression, String templateId,
                                 Boolean isActive, Integer priority, LocalDateTime createdAt, LocalDateTime updatedAt,
                                 String createdBy, String updatedBy) {
        return new JiraJqlQuery(id, queryName, jqlExpression, templateId, isActive, priority,
                          createdAt, updatedAt, createdBy, updatedBy);
    }
    
    /**
     * JQLクエリ作成パラメータの検証
     */
    private static void validateCreateParameters(String queryName, String jqlExpression, String templateId,
                                               Integer priority, String createdBy) {
        // クエリ名の検証
        if (queryName == null || queryName.trim().isEmpty()) {
            throw new IllegalArgumentException("クエリ名は必須です");
        }
        
        // JQL式の検証
        if (jqlExpression == null || jqlExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("JQL式は必須です");
        }
        
        // テンプレートIDの検証
        if (templateId == null || templateId.trim().isEmpty()) {
            throw new IllegalArgumentException("テンプレートIDは必須です");
        }
        
        // 優先度の検証
        if (priority == null || priority < 0) {
            throw new IllegalArgumentException("優先度は0以上である必要があります");
        }
        
        // 作成者の検証
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("作成者は必須です");
        }
    }
    
    /**
     * 更新パラメータの共通検証
     */
    private void validateUpdatedBy(String updatedBy) {
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("更新者は必須です");
        }
    }
    
    /**
     * JQLクエリをアクティブ化
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * JQLクエリを非アクティブ化
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 実行優先度を更新
     * 
     * @param newPriority 新しい優先度（0以上）
     * @param updatedBy 更新者ID
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updatePriority(Integer newPriority, String updatedBy) {
        if (newPriority == null || newPriority < 0) {
            throw new IllegalArgumentException("優先度は0以上である必要があります");
        }
        
        validateUpdatedBy(updatedBy);
        
        this.priority = newPriority;
        this.updatedBy = updatedBy.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * クエリ名を更新
     * 
     * @param newQueryName 新しいクエリ名
     * @param updatedBy 更新者ID
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateQueryName(String newQueryName, String updatedBy) {
        if (newQueryName == null || newQueryName.trim().isEmpty()) {
            throw new IllegalArgumentException("クエリ名は必須です");
        }
        
        validateUpdatedBy(updatedBy);
        
        this.queryName = newQueryName.trim();
        this.updatedBy = updatedBy.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * JQL式を更新
     * 
     * @param newJqlExpression 新しいJQL式
     * @param updatedBy 更新者ID
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateQuery(String newJqlExpression, String updatedBy) {
        if (newJqlExpression == null || newJqlExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("JQL式は必須です");
        }
        
        validateUpdatedBy(updatedBy);
        
        this.jqlExpression = newJqlExpression.trim();
        this.updatedBy = updatedBy.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * テンプレートIDを更新
     * 
     * @param newTemplateId 新しいテンプレートID
     * @param updatedBy 更新者ID
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateTemplate(String newTemplateId, String updatedBy) {
        if (newTemplateId == null || newTemplateId.trim().isEmpty()) {
            throw new IllegalArgumentException("テンプレートIDは必須です");
        }
        
        validateUpdatedBy(updatedBy);
        
        this.templateId = newTemplateId.trim();
        this.updatedBy = updatedBy.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 指定されたJQLクエリより高い優先度を持つかチェック
     * 
     * @param other 比較対象のJQLクエリ
     * @return このクエリの方が高い優先度の場合true
     */
    public boolean isHigherPriorityThan(JiraJqlQuery other) {
        if (other == null) {
            return true;
        }
        return this.priority > other.priority;
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getQueryName() { return queryName; }
    public String getJqlExpression() { return jqlExpression; }
    public String getTemplateId() { return templateId; }
    public Boolean isActive() { return isActive; }
    public Integer getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    
    // MyBatis専用セッター（パッケージプライベート）
    // ドメインロジックからは使用しない
    void setId(String id) { this.id = id; }
    void setQueryName(String queryName) { this.queryName = queryName; }
    void setJqlExpression(String jqlExpression) { this.jqlExpression = jqlExpression; }
    void setTemplateId(String templateId) { this.templateId = templateId; }
    void setIsActive(Boolean isActive) { this.isActive = isActive; }
    void setPriority(Integer priority) { this.priority = priority; }
    void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JiraJqlQuery jqlQuery = (JiraJqlQuery) obj;
        return Objects.equals(id, jqlQuery.id);
    }
    
    /**
     * ハッシュコード（IDベース）
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 文字列表現
     */
    @Override
    public String toString() {
        return String.format("JqlQuery{id='%s', queryName='%s', priority=%d, isActive=%s}", 
                           id, queryName, priority, isActive);
    }
}