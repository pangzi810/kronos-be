package com.devhour.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * レスポンステンプレートエンティティ
 * 
 * JIRA同期機能で使用するVelocityテンプレートを管理するエンティティ
 * JIRAAPIレスポンスを共通フォーマットに変換するためのテンプレート情報を保持する
 * 
 * 責務:
 * - Velocityテンプレート情報の管理
 * - テンプレート構文の検証
 * - フィールド参照の検出
 * - テンプレートの更新管理
 */
public class JiraResponseTemplate {
    
    private String id;
    private String templateName;
    private String velocityTemplate;
    private String templateDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private JiraResponseTemplate() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }

    /**
     * プライベートコンストラクタ（新規作成用）
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private JiraResponseTemplate(String id, String templateName, String velocityTemplate, 
                           String templateDescription, LocalDateTime createdAt) {
        this.id = id;
        this.templateName = templateName;
        this.velocityTemplate = velocityTemplate;
        this.templateDescription = templateDescription;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
    
    /**
     * 既存レスポンステンプレートの復元用コンストラクタ
     * リポジトリからの読み込み時に使用
     */
    private JiraResponseTemplate(String id, String templateName, String velocityTemplate,
                           String templateDescription, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.templateName = templateName;
        this.velocityTemplate = velocityTemplate;
        this.templateDescription = templateDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 新しいレスポンステンプレートを作成するファクトリーメソッド
     * 
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @param templateDescription テンプレート説明（nullable）
     * @return 新しいResponseTemplateエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static JiraResponseTemplate createNew(String templateName, String velocityTemplate, String templateDescription) {
        validateCreateParameters(templateName, velocityTemplate);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        // 説明の正規化（空白のみの場合はnullに）
        String normalizedDescription = templateDescription != null && !templateDescription.trim().isEmpty() 
            ? templateDescription.trim() 
            : null;
        
        JiraResponseTemplate template = new JiraResponseTemplate(id, templateName.trim(), velocityTemplate.trim(), 
                                                       normalizedDescription, now);
        
        // Velocityテンプレートの構文検証
        template.validateVelocityTemplate();
        
        return template;
    }
    
    /**
     * 既存レスポンステンプレートを復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     * 
     * @param id テンプレートID
     * @param templateName テンプレート名
     * @param velocityTemplate Velocityテンプレート
     * @param templateDescription テンプレート説明
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return 復元されたResponseTemplateエンティティ
     */
    public static JiraResponseTemplate restore(String id, String templateName, String velocityTemplate,
                                         String templateDescription, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new JiraResponseTemplate(id, templateName, velocityTemplate, templateDescription,
                                  createdAt, updatedAt);
    }
    
    /**
     * レスポンステンプレート作成パラメータの検証
     */
    private static void validateCreateParameters(String templateName, String velocityTemplate) {
        // テンプレート名の検証
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("テンプレート名は必須です");
        }
        
        // Velocityテンプレートの検証
        if (velocityTemplate == null || velocityTemplate.isEmpty()) {
            throw new IllegalArgumentException("Velocityテンプレートは必須です");
        }
        if (velocityTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Velocityテンプレートが空です");
        }
    }
    
    /**
     * Velocityテンプレートの更新
     * 
     * @param newVelocityTemplate 新しいVelocityテンプレート
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateTemplate(String newVelocityTemplate) {
        if (newVelocityTemplate == null || newVelocityTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Velocityテンプレートが空です");
        }
        
        String trimmedTemplate = newVelocityTemplate.trim();
        
        // 新しいテンプレートの構文検証
        validateVelocityTemplateSyntax(trimmedTemplate);
        
        this.velocityTemplate = trimmedTemplate;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * テンプレート名の更新
     * 
     * @param newTemplateName 新しいテンプレート名
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateName(String newTemplateName) {
        if (newTemplateName == null || newTemplateName.trim().isEmpty()) {
            throw new IllegalArgumentException("テンプレート名は必須です");
        }
        
        this.templateName = newTemplateName.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * テンプレート説明の更新
     * 
     * @param newDescription 新しいテンプレート説明
     */
    public void updateDescription(String newDescription) {
        // 説明の正規化（空白のみの場合はnullに）
        this.templateDescription = newDescription != null && !newDescription.trim().isEmpty() 
            ? newDescription.trim() 
            : null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Velocityテンプレートの基本構文検証
     * 現在のテンプレートの構文をチェックする
     * 
     * @throws IllegalArgumentException 構文エラーがある場合
     */
    public void validateVelocityTemplate() {
        validateVelocityTemplateSyntax(this.velocityTemplate);
    }
    
    /**
     * Velocityテンプレート構文検証の実装
     * 基本的な構文エラーのみをチェックする
     * 
     * @param template 検証するテンプレート
     * @throws IllegalArgumentException 構文エラーがある場合
     */
    private void validateVelocityTemplateSyntax(String template) {
        // 基本的な構文検証のみ実装 - 詳細検証は今後の課題
        // TODO: より高度な構文検証の実装
        
        // テンプレートが最低限の形式に従っているかのみチェック
        if (template.trim().isEmpty()) {
            throw new IllegalArgumentException("Velocityテンプレートが空です");
        }
    }
    
    /**
     * 指定されたフィールドがテンプレートに含まれているかチェック
     * 
     * @param fieldName チェックするフィールド名
     * @return テンプレートに含まれている場合true
     */
    public boolean containsField(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return false;
        }
        
        // 単純な文字列検索でフィールド名が含まれているかチェック
        return this.velocityTemplate.contains(fieldName);
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getTemplateName() { return templateName; }
    public String getVelocityTemplate() { return velocityTemplate; }
    public String getTemplateDescription() { return templateDescription; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // MyBatisマッピング用のパッケージプライベートセッター
    void setId(String id) { this.id = id; }
    void setTemplateName(String templateName) { this.templateName = templateName; }
    void setVelocityTemplate(String velocityTemplate) { this.velocityTemplate = velocityTemplate; }
    void setTemplateDescription(String templateDescription) { this.templateDescription = templateDescription; }
    void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JiraResponseTemplate template = (JiraResponseTemplate) obj;
        return Objects.equals(id, template.id);
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
        return String.format("ResponseTemplate{id='%s', templateName='%s'}", 
                           id, templateName);
    }
}