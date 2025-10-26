package com.devhour.domain.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import com.devhour.domain.model.valueobject.ProjectStatus;

/**
 * プロジェクトエンティティ
 * 
 * ドメインの中核となるエンティティで、プロジェクトのライフサイクルと
 * ビジネスルールを管理する。JIRA統合機能も含む。
 * 
 * 責務:
 * - プロジェクト基本情報の管理
 * - プロジェクトステータス遷移の制御
 * - プロジェクト期間の検証
 * - ビジネスルールの実装
 * - JIRA統合管理（JIRAイシューキーによる統合）
 */
public class Project {
    
    /**
     * JIRAイシューキーの形式検証用正規表現
     * 形式: プロジェクトキー（大文字英数字とアンダースコア）-番号（1以上の数字）
     * 例: PROJ-123, DEVHOUR-456, TEST_PROJECT-789
     */
    private static final Pattern JIRA_ISSUE_KEY_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*-[1-9]\\d*$");
    
    private String id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;
    private ProjectStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String jiraIssueKey;
    private String customFields;
    
    private Project() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }

    /**
     * プライベートコンストラクタ（JIRA統合なし）
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private Project(String id, String name, String description, LocalDate startDate, 
                   LocalDate plannedEndDate, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.plannedEndDate = plannedEndDate;
        this.actualEndDate = null;
        this.status = ProjectStatus.DRAFT;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.jiraIssueKey = null;
    }
    
    /**
     * プライベートコンストラクタ（JIRA統合あり）
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private Project(String id, String name, String description, LocalDate startDate, 
                   LocalDate plannedEndDate, String createdBy, LocalDateTime createdAt,
                   String jiraIssueKey, String customFields) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.plannedEndDate = plannedEndDate;
        this.actualEndDate = null;
        this.status = ProjectStatus.DRAFT;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.jiraIssueKey = jiraIssueKey;
        this.customFields = customFields;
    }
    
    /**
     * 既存プロジェクトの復元用コンストラクタ
     * リポジトリからの読み込み時に使用
     */
    private Project(String id, String name, String description, LocalDate startDate,
                   LocalDate plannedEndDate, LocalDate actualEndDate, ProjectStatus status,
                   String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt,
                   String jiraIssueKey, String customFields) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.plannedEndDate = plannedEndDate;
        this.actualEndDate = actualEndDate;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.jiraIssueKey = jiraIssueKey;
        this.customFields = customFields;
    }
    
    /**
     * 新しいプロジェクトを作成するファクトリーメソッド（JIRA統合なし）
     * 
     * @param name プロジェクト名
     * @param description プロジェクト説明
     * @param startDate 開始日
     * @param plannedEndDate 予定終了日
     * @param createdBy 作成者ID
     * @return 新しいProjectエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static Project create(String name, String description, LocalDate startDate,
                               LocalDate plannedEndDate, String createdBy) {
        return create(name, description, startDate, plannedEndDate, createdBy, null, null);
    }
    
    /**
     * 新しいプロジェクトを作成するファクトリーメソッド（JIRA統合対応）
     * 
     * @param name プロジェクト名
     * @param description プロジェクト説明
     * @param startDate 開始日
     * @param plannedEndDate 予定終了日
     * @param createdBy 作成者ID
     * @param jiraIssueKey JIRAイシューキー（可選）
     * @return 新しいProjectエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static Project create(String name, String description, LocalDate startDate,
                               LocalDate plannedEndDate, String createdBy, String jiraIssueKey, String customFields) {
        validateCreateParameters(name, startDate, plannedEndDate, createdBy);
        validateJiraIssueKey(jiraIssueKey);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new Project(id, name, description, startDate, plannedEndDate, createdBy, now, jiraIssueKey, customFields);
    }
    
    /**
     * 既存プロジェクトを復元するファクトリーメソッド（後方互換性）
     * リポジトリからの読み込み時に使用
     * 
     * @param id プロジェクトID
     * @param name プロジェクト名
     * @param description プロジェクト説明
     * @param startDate 開始日
     * @param plannedEndDate 予定終了日
     * @param actualEndDate 実際の終了日
     * @param status プロジェクトステータス
     * @param createdBy 作成者ID
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return 復元されたProjectエンティティ
     */
    public static Project restore(String id, String name, String description, LocalDate startDate,
                                LocalDate plannedEndDate, LocalDate actualEndDate, ProjectStatus status,
                                String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return restore(id, name, description, startDate, plannedEndDate, actualEndDate, 
                     status, createdBy, createdAt, updatedAt, null, null);
    }
    
    /**
     * 既存プロジェクトを復元するファクトリーメソッド（JIRA統合対応）
     * リポジトリからの読み込み時に使用
     * 
     * @param id プロジェクトID
     * @param name プロジェクト名
     * @param description プロジェクト説明
     * @param startDate 開始日
     * @param plannedEndDate 予定終了日
     * @param actualEndDate 実際の終了日
     * @param status プロジェクトステータス
     * @param createdBy 作成者ID
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @param jiraIssueKey JIRAイシューキー（可選）
     * @return 復元されたProjectエンティティ
     */
    public static Project restore(String id, String name, String description, LocalDate startDate,
                                LocalDate plannedEndDate, LocalDate actualEndDate, ProjectStatus status,
                                String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt,
                                String jiraIssueKey, String customFields) {
        return new Project(id, name, description, startDate, plannedEndDate, actualEndDate,
                         status, createdBy, createdAt, updatedAt, jiraIssueKey, customFields);
    }
    
    /**
     * プロジェクト作成パラメータの検証
     */
    private static void validateCreateParameters(String name, LocalDate startDate,
                                               LocalDate plannedEndDate, String createdBy) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("プロジェクト名は必須です");
        }
        
        if (name.trim().length() > 255) {
            throw new IllegalArgumentException("プロジェクト名は255文字以内で入力してください");
        }
        
        if (startDate == null) {
            throw new IllegalArgumentException("開始日は必須です");
        }
        
        if (plannedEndDate == null) {
            throw new IllegalArgumentException("予定終了日は必須です");
        }
        
        if (plannedEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("予定終了日は開始日以降の日付を設定してください");
        }
        
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("作成者IDは必須です");
        }
    }
    
    /**
     * JIRAイシューキーの検証
     * 
     * @param jiraIssueKey 検証対象のJIRAイシューキー
     * @throws IllegalArgumentException フォーマットが不正な場合
     */
    private static void validateJiraIssueKey(String jiraIssueKey) {
        if (jiraIssueKey == null) {
            return; // nullは許可（JIRA統合なし）
        }
        
        String trimmedKey = jiraIssueKey.trim();
        if (trimmedKey.isEmpty() || !JIRA_ISSUE_KEY_PATTERN.matcher(trimmedKey).matches()) {
            throw new IllegalArgumentException("JIRAイシューキーの形式が正しくありません。" +
                "形式: PROJECT-123 (プロジェクトキーは大文字英数字とアンダースコア、番号は1以上)");
        }
    }
    
    /**
     * プロジェクト情報を更新
     * 
     * @param name 新しいプロジェクト名
     * @param description 新しい説明
     * @param startDate 新しい開始日
     * @param plannedEndDate 新しい予定終了日
     * @throws IllegalArgumentException パラメータエラーの場合
     * @throws IllegalStateException 更新不可能な状態の場合
     */
    public void updateProjectInfo(String name, String description, LocalDate startDate, LocalDate plannedEndDate) {
        if (status.isClosed()) {
            throw new IllegalStateException("完了または中止されたプロジェクトは更新できません");
        }

        validateUpdateParameters(name, startDate, plannedEndDate);

        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.startDate = startDate;
        this.plannedEndDate = plannedEndDate;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * プロジェクト更新パラメータの検証
     */
    private void validateUpdateParameters(String name, LocalDate startDate, LocalDate plannedEndDate) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("プロジェクト名は必須です");
        }
        
        if (name.trim().length() > 255) {
            throw new IllegalArgumentException("プロジェクト名は255文字以内で入力してください");
        }
        
        if (startDate == null) {
            throw new IllegalArgumentException("開始日は必須です");
        }
        
        if (plannedEndDate == null) {
            throw new IllegalArgumentException("予定終了日は必須です");
        }
        
        if (plannedEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("予定終了日は開始日以降の日付を設定してください");
        }
    }
    
    /**
     * プロジェクトを開始
     * 
     * @throws IllegalStateException 開始できない状態の場合
     */
    public void start() {
        if (!status.canStart()) {
            throw new IllegalStateException(
                String.format("プロジェクトを開始できません。現在のステータス: %s", status.getDisplayName())
            );
        }
        
        this.status = status.transitionTo(ProjectStatus.IN_PROGRESS);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * プロジェクトを完了
     * 
     * @param actualEndDate 実際の終了日
     * @throws IllegalStateException 完了できない状態の場合
     * @throws IllegalArgumentException 不正な終了日の場合
     */
    public void close(LocalDate actualEndDate) {
        if (!status.isInProgress()) {
            throw new IllegalStateException(
                String.format("プロジェクトを完了できません。現在のステータス: %s", status.getDisplayName())
            );
        }
        
        if (actualEndDate == null) {
            throw new IllegalArgumentException("実際の終了日は必須です");
        }
        
        if (actualEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("実際の終了日は開始日以降の日付を設定してください");
        }
        
        this.status = status.transitionTo(ProjectStatus.CLOSED);
        this.actualEndDate = actualEndDate;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * プロジェクトが工数記録可能な状態かチェック
     * 
     * @return 工数記録可能な場合true
     */
    public boolean canRecordWorkHours() {
        return status.isActive();
    }
    
    /**
     * プロジェクト期間内の日付かチェック
     * 
     * @param date チェック対象の日付
     * @return プロジェクト期間内の場合true
     */
    public boolean isDateWithinProjectPeriod(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        // 開始日以降かチェック
        if (date.isBefore(startDate)) {
            return false;
        }
        
        // 終了日以前かチェック（実際の終了日が設定されている場合はそれを使用）
        LocalDate endDate = actualEndDate != null ? actualEndDate : plannedEndDate;
        return !date.isAfter(endDate);
    }
    
    // ========== JIRA統合機能 ==========
    
    /**
     * JIRAイシューキーを割り当て
     * 
     * @param jiraIssueKey JIRAイシューキー
     * @throws IllegalArgumentException 不正なイシューキーの場合
     */
    public void assignJiraIssueKey(String jiraIssueKey) {
        if (jiraIssueKey == null) {
            throw new IllegalArgumentException("JIRAイシューキーは必須です");
        }
        
        validateJiraIssueKey(jiraIssueKey);
        this.jiraIssueKey = jiraIssueKey.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * JIRA統合を解除
     * JIRAイシューキーを削除し、非統合状態にする
     */
    public void removeJiraIntegration() {
        this.jiraIssueKey = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * JIRA統合されているかチェック
     * 
     * @return JIRA統合されている場合true
     */
    public boolean hasJiraIntegration() {
        return jiraIssueKey != null;
    }
    
    /**
     * JIRAプロジェクトかどうかチェック
     * hasJiraIntegration()のエイリアス
     * 
     * @return JIRA統合されている場合true
     */
    public boolean isJiraProject() {
        return hasJiraIntegration();
    }
    
    /**
     * JIRAから取得した情報でプロジェクト情報を更新
     * 
     * @param name 新しいプロジェクト名
     * @param description 新しい説明
     * @param startDate 新しい開始日
     * @param plannedEndDate 新しい予定終了日
     * @param status 新しいプロジェクトステータス
     * @throws IllegalStateException JIRA統合されていない、または更新不可能な状態の場合
     * @throws IllegalArgumentException パラメータエラーの場合
     */
    public void updateFromJira(String name, String description, LocalDate startDate,
                              LocalDate plannedEndDate, ProjectStatus status, String customFields) {
        if (!hasJiraIntegration()) {
            throw new IllegalStateException("JIRA統合されていないプロジェクトは、JIRAからの情報更新はできません");
        }

        if (this.status.isClosed()) {
            throw new IllegalStateException("完了または中止されたプロジェクトは、JIRAからの情報更新はできません");
        }

        validateUpdateParameters(name, startDate, plannedEndDate);
        
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.startDate = startDate;
        this.plannedEndDate = plannedEndDate;
        this.status = status;
        this.customFields = customFields;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 他のプロジェクトと同じJIRAイシューキーを持つかチェック
     * 
     * @param other 比較対象のプロジェクト
     * @return 同じJIRAイシューキーの場合true、どちらかがnullの場合false
     */
    public boolean hasSameJiraIssue(Project other) {
        if (other == null) {
            return false;
        }
        return hasSameJiraIssue(other.jiraIssueKey);
    }
    
    /**
     * 指定されたJIRAイシューキーと同じかチェック
     * 
     * @param jiraIssueKey 比較対象のJIRAイシューキー
     * @return 同じJIRAイシューキーの場合true
     */
    public boolean hasSameJiraIssue(String jiraIssueKey) {
        if (this.jiraIssueKey == null && jiraIssueKey == null) {
            return true;
        }
        if (this.jiraIssueKey == null || jiraIssueKey == null) {
            return false;
        }
        return this.jiraIssueKey.equals(jiraIssueKey);
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getPlannedEndDate() { return plannedEndDate; }
    public LocalDate getActualEndDate() { return actualEndDate; }
    public ProjectStatus getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getJiraIssueKey() { return jiraIssueKey; }
    public String getCustomFields() { return customFields; }
    
    // MyBatis用のpackage-privateセッター
    void setId(String id) { this.id = id; }
    void setName(String name) { this.name = name; }
    void setDescription(String description) { this.description = description; }
    void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    void setPlannedEndDate(LocalDate plannedEndDate) { this.plannedEndDate = plannedEndDate; }
    void setActualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; }
    void setStatus(ProjectStatus status) { this.status = status; }
    void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    void setJiraIssueKey(String jiraIssueKey) { this.jiraIssueKey = jiraIssueKey; }
    void setCustomFields(String customFields) { this.customFields = customFields; }
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return Objects.equals(id, project.id);
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
        return String.format("Project{id='%s', name='%s', status=%s, jiraIssueKey=%s}", 
                           id, name, status, jiraIssueKey != null ? "'" + jiraIssueKey + "'" : "null");
    }
}