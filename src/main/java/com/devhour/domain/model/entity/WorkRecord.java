package com.devhour.domain.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.devhour.domain.model.valueobject.CategoryHours;

/**
 * 工数記録エンティティ
 * 
 * 開発者の日次工数記録を管理するエンティティ
 * 1日1レコード方式でカテゴリ別の工数を管理
 * 
 * 責務:
 * - 日次工数記録の管理
 * - カテゴリ別工数の管理
 * - 工数記録の検証ロジック
 * - 1日1レコード制御
 */
public class WorkRecord {
    
    private String id;
    private String userId;
    private String projectId;
    private LocalDate workDate;
    private CategoryHours categoryHours;
    private String description;
    // 承認関連フィールドはwork_record_approvalテーブルで管理
    private String createdBy; // 作成者
    private LocalDateTime createdAt;
    private String updatedBy; // 更新者
    private LocalDateTime updatedAt;

    private Project project; // 関連プロジェクト（オプション）
    private User user;
    
    private WorkRecord() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }
    
    /**
     * プライベートコンストラクタ
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private WorkRecord(String id, String userId, String projectId, LocalDate workDate,
                      CategoryHours categoryHours, String description, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.workDate = workDate;
        this.categoryHours = categoryHours;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = createdBy;
        this.updatedAt = createdAt;
    }
    
    /**
     * 既存工数記録の復元用コンストラクタ
     * リポジトリからの読み込み時に使用
     */
    private WorkRecord(String id, String userId, String projectId, LocalDate workDate,
                      CategoryHours categoryHours, String description,
                      String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.workDate = workDate;
        this.categoryHours = categoryHours;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 新しい工数記録を作成するファクトリーメソッド
     * 
     * @param userId ユーザーID（開発者）
     * @param projectId プロジェクトID
     * @param workDate 作業日
     * @param categoryHours カテゴリ別工数
     * @param description 作業内容・備考
     * @return 新しいWorkRecordエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static WorkRecord create(String userId, String projectId, LocalDate workDate,
                                  CategoryHours categoryHours, String description, String createdBy) {
        validateCreateParameters(userId, projectId, workDate, categoryHours);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return new WorkRecord(id, userId, projectId, workDate, categoryHours, description, createdBy, now);
    }
    
    /**
     * 既存工数記録を復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     * 
     * @param id 工数記録ID
     * @param userId ユーザーID
     * @param projectId プロジェクトID
     * @param workDate 作業日
     * @param categoryHours カテゴリ別工数
     * @param description 作業内容・備考
     * @param createdBy 作成者ID
     * @param createdAt 作成日時
     * @param updatedBy 更新者ID
     * @param updatedAt 更新日時
     * @return 復元されたWorkRecordエンティティ
     */
    public static WorkRecord restore(String id, String userId, String projectId, LocalDate workDate,
                                   CategoryHours categoryHours, String description,
                                   String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {
        return new WorkRecord(id, userId, projectId, workDate, categoryHours, description,
                             createdBy, createdAt, updatedBy, updatedAt);
    }
    
    /**
     * 工数記録作成パラメータの検証
     */
    private static void validateCreateParameters(String userId, String projectId, LocalDate workDate,
                                               CategoryHours categoryHours) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("ユーザーIDは必須です");
        }
        
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("プロジェクトIDは必須です");
        }
        
        if (workDate == null) {
            throw new IllegalArgumentException("作業日は必須です");
        }
        
        // 未来日チェック（当日まで許可）
        if (workDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("作業日は当日以前の日付を入力してください");
        }
        
        // 過去90日以内のチェック（データ品質確保）
        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        if (workDate.isBefore(ninetyDaysAgo)) {
            throw new IllegalArgumentException("作業日は過去90日以内の日付を入力してください");
        }
        
        if (categoryHours == null) {
            throw new IllegalArgumentException("カテゴリ別工数は必須です");
        }
        
        // 工数が0の場合はエラー（何かしらの工数が必要）
        if (!categoryHours.hasAnyHours()) {
            throw new IllegalArgumentException("少なくとも1つのカテゴリに工数を入力してください");
        }
        
        // 1日の合計工数上限チェック（24時間以内）は削除 - 集計機能は不要
    }
    
    /**
     * 工数記録を更新
     * 
     * @param newCategoryHours 新しいカテゴリ別工数
     * @param newDescription 新しい作業内容・備考
     * @param updatedBy 更新者ID
     * @throws IllegalArgumentException パラメータエラーの場合
     * @throws IllegalStateException 更新不可能な状態の場合
     */
    public void updateWorkRecord(CategoryHours newCategoryHours, String newDescription, String updatedBy) {
        validateUpdateParameters(newCategoryHours);
        
        this.categoryHours = newCategoryHours;
        this.description = newDescription != null ? newDescription.trim() : null;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 工数記録更新パラメータの検証
     */
    private void validateUpdateParameters(CategoryHours newCategoryHours) {
        if (newCategoryHours == null) {
            throw new IllegalArgumentException("カテゴリ別工数は必須です");
        }
        
        // 工数が0の場合はエラー
        if (!newCategoryHours.hasAnyHours()) {
            throw new IllegalArgumentException("少なくとも1つのカテゴリに工数を入力してください");
        }
        
        // 1日の合計工数上限チェック
        // 1日の合計工数上限チェックは削除 - 集計機能は不要
        
        // 過去の記録の更新可能期間チェック（作成から30日以内）
        LocalDate thirtyDaysAfterCreated = createdAt.toLocalDate().plusDays(30);
        if (LocalDate.now().isAfter(thirtyDaysAfterCreated)) {
            throw new IllegalStateException("工数記録は作成から30日以内のみ更新可能です");
        }
    }
    

    
    /**
     * 工数記録が更新可能な期間内かチェック
     * 
     * @return 更新可能な場合true
     */
    public boolean isUpdatable() {
        LocalDate thirtyDaysAfterCreated = createdAt.toLocalDate().plusDays(30);
        return !LocalDate.now().isAfter(thirtyDaysAfterCreated);
    }
    
    
    
    
    
    /**
     * 指定した作業日の工数記録かチェック
     * 
     * @param date チェック対象の日付
     * @return 指定日の記録の場合true
     */
    public boolean isRecordForDate(LocalDate date) {
        return workDate.equals(date);
    }
    
    /**
     * 指定したユーザーの工数記録かチェック
     * 
     * @param userId チェック対象のユーザーID
     * @return 指定ユーザーの記録の場合true
     */
    public boolean belongsToUser(String userId) {
        return this.userId.equals(userId);
    }
    
    /**
     * 指定したプロジェクトの工数記録かチェック
     * 
     * @param projectId チェック対象のプロジェクトID
     * @return 指定プロジェクトの記録の場合true
     */
    public boolean belongsToProject(String projectId) {
        return this.projectId.equals(projectId);
    }
    
    /**
     * 工数記録の週を取得
     * 
     * @return 作業日が属する週の月曜日の日付
     */
    public LocalDate getWeekStartDate() {
        return workDate.minusDays(workDate.getDayOfWeek().getValue() - 1);
    }
    
    /**
     * 説明文の設定/更新
     * 
     * @param newDescription 新しい説明文
     * @param updatedBy 更新者ID
     */
    public void updateDescription(String newDescription, String updatedBy) {
        this.description = newDescription != null ? newDescription.trim() : null;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ゲッター
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getProjectId() { return projectId; }
    public LocalDate getWorkDate() { return workDate; }
    public CategoryHours getCategoryHours() { return categoryHours; }
    public String getDescription() { return description; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Project getProject() { return project; }
    public User getUser() { return user; }
    
    // セッター
    public void setProject(Project project) { this.project = project; }
    public void setUser(User user) { this.user = user; }
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkRecord that = (WorkRecord) obj;
        return Objects.equals(id, that.id);
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
        return String.format("WorkRecord{id='%s', userId='%s', projectId='%s', workDate=%s}", 
            id, userId, projectId, workDate);
    }
}