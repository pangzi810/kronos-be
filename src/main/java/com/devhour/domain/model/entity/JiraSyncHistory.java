package com.devhour.domain.model.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.devhour.domain.model.valueobject.DetailStatus;
import com.devhour.domain.model.valueobject.JiraSyncStatus;
import com.devhour.domain.model.valueobject.JiraSyncType;

/**
 * 同期履歴エンティティ
 * 
 * JIRA同期実行の履歴とサマリ情報を管理するエンティティ
 * 
 * 責務:
 * - 同期実行の開始・完了・失敗の管理
 * - 処理結果のサマリ情報管理（総数、成功数、エラー数）
 * - 実行時間とパフォーマンス情報の管理
 * - 詳細履歴との関連付け管理
 */
public class JiraSyncHistory {
    
    private String id;
    private JiraSyncType syncType;
    private JiraSyncStatus syncStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer totalProjectsProcessed;
    private Integer successCount;
    private Integer errorCount;
    private String errorDetails;
    private String triggeredBy;
    
    // 関連する詳細履歴のコレクション（遅延読み込み用）
    private List<JiraSyncHistoryDetail> details;
    
    private JiraSyncHistory() {
        // MyBatisのマッピング用に必要
        // デフォルトコンストラクタは使用しない
        // ファクトリーメソッドからのみインスタンス化される
    }

    /**
     * プライベートコンストラクタ（新規作成用）
     * ファクトリーメソッドからのみ呼び出し可能
     */
    private JiraSyncHistory(String id, JiraSyncType syncType, String triggeredBy, LocalDateTime startedAt) {
        this.id = id;
        this.syncType = syncType;
        this.syncStatus = JiraSyncStatus.IN_PROGRESS;
        this.startedAt = startedAt;
        this.completedAt = null;
        this.totalProjectsProcessed = 0;
        this.successCount = 0;
        this.errorCount = 0;
        this.errorDetails = null;
        this.triggeredBy = triggeredBy;
        this.details = new ArrayList<>();
    }
    
    /**
     * 既存同期履歴の復元用コンストラクタ
     * リポジトリからの読み込み時に使用
     */
    private JiraSyncHistory(String id, JiraSyncType syncType, JiraSyncStatus syncStatus, LocalDateTime startedAt,
                       LocalDateTime completedAt, Integer totalProjectsProcessed, Integer successCount,
                       Integer errorCount, String errorDetails, String triggeredBy) {
        this.id = id;
        this.syncType = syncType;
        this.syncStatus = syncStatus;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.totalProjectsProcessed = totalProjectsProcessed;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.errorDetails = errorDetails;
        this.triggeredBy = triggeredBy;
        this.details = new ArrayList<>();
    }
    
    /**
     * 新しい同期履歴を開始するファクトリーメソッド
     * 
     * @param syncType 同期タイプ
     * @param triggeredBy 実行者/トリガー（nullable）
     * @return 新しいSyncHistoryエンティティ
     * @throws IllegalArgumentException ビジネスルール違反の場合
     */
    public static JiraSyncHistory startSync(JiraSyncType syncType, String triggeredBy) {
        validateSyncType(syncType);
        
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        // triggeredByの正規化（空白のみの場合はnullに）
        String normalizedTriggeredBy = triggeredBy != null && !triggeredBy.trim().isEmpty() 
            ? triggeredBy.trim() 
            : null;
        
        return new JiraSyncHistory(id, syncType, normalizedTriggeredBy, now);
    }
    
    /**
     * 既存同期履歴を復元するファクトリーメソッド
     * リポジトリからの読み込み時に使用
     * 
     * @param id 同期履歴ID
     * @param syncType 同期タイプ
     * @param syncStatus 同期ステータス
     * @param startedAt 開始日時
     * @param completedAt 完了日時
     * @param totalProjectsProcessed 処理プロジェクト総数
     * @param successCount 成功数
     * @param errorCount エラー数
     * @param errorDetails エラー詳細
     * @param triggeredBy 実行者/トリガー
     * @return 復元されたSyncHistoryエンティティ
     */
    public static JiraSyncHistory restore(String id, JiraSyncType syncType, JiraSyncStatus syncStatus, 
                                     LocalDateTime startedAt, LocalDateTime completedAt,
                                     Integer totalProjectsProcessed, Integer successCount, Integer errorCount,
                                     String errorDetails, String triggeredBy) {
        return new JiraSyncHistory(id, syncType, syncStatus, startedAt, completedAt,
                              totalProjectsProcessed, successCount, errorCount, errorDetails, triggeredBy);
    }
    
    /**
     * 同期タイプの検証
     */
    private static void validateSyncType(JiraSyncType syncType) {
        if (syncType == null) {
            throw new IllegalArgumentException("同期タイプは必須です");
        }
    }
    
    /**
     * 同期を完了状態にする
     * 
     * @throws IllegalStateException 実行中以外の状態で呼び出された場合
     */
    public void completeSync() {
        if (!syncStatus.isInProgress()) {
            throw new IllegalStateException("実行中ではない同期を完了することはできません。現在のステータス: " + syncStatus.getDisplayName());
        }
        
        this.syncStatus = JiraSyncStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 同期を失敗状態にする
     * 
     * @param errorDetails エラー詳細（nullable）
     * @throws IllegalStateException 実行中以外の状態で呼び出された場合
     */
    public void failSync(String errorDetails) {
        if (!syncStatus.isInProgress()) {
            throw new IllegalStateException("実行中ではない同期を失敗させることはできません。現在のステータス: " + syncStatus.getDisplayName());
        }
        
        this.syncStatus = JiraSyncStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        
        // エラー詳細の正規化（空白のみの場合はnullに）
        this.errorDetails = errorDetails != null && !errorDetails.trim().isEmpty() 
            ? errorDetails.trim() 
            : null;
    }
    
    /**
     * 処理プロジェクト数を増加
     */
    public void incrementProcessed() {
        if (!syncStatus.isInProgress()) {
            throw new IllegalStateException("実行中ではない同期のカウントを変更することはできません");
        }
        this.totalProjectsProcessed++;
    }
    
    /**
     * 成功数を増加
     */
    public void incrementSuccess() {
        if (!syncStatus.isInProgress()) {
            throw new IllegalStateException("実行中ではない同期のカウントを変更することはできません");
        }

        // 成功数とエラー数の合計が処理総数を超えないかチェック
        if (this.successCount + 1 + this.errorCount > this.totalProjectsProcessed) {
            throw new IllegalStateException(String.format(
                "成功数とエラー数の合計が処理総数を超えています。総数: %d, 成功: %d, エラー: %d",
                this.totalProjectsProcessed, this.successCount + 1, this.errorCount));
        }

        this.successCount++;
    }
    
    /**
     * エラー数を増加
     */
    public void incrementError() {
        if (!syncStatus.isInProgress()) {
            throw new IllegalStateException("実行中ではない同期のカウントを変更することはできません");
        }

        // 成功数とエラー数の合計が処理総数を超えないかチェック
        if (this.successCount + this.errorCount + 1 > this.totalProjectsProcessed) {
            throw new IllegalStateException(String.format(
                "成功数とエラー数の合計が処理総数を超えています。総数: %d, 成功: %d, エラー: %d",
                this.totalProjectsProcessed, this.successCount, this.errorCount + 1));
        }

        this.errorCount++;
    }
    
    /**
     * 詳細履歴を追加
     * 
     * @param detail 詳細履歴
     * @throws IllegalArgumentException 詳細履歴がnullまたは異なる同期履歴IDの場合
     */
    private void addDetail(JiraSyncHistoryDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("詳細履歴はnullにできません");
        }
        
        if (!Objects.equals(this.id, detail.getSyncHistoryId())) {
            throw new IllegalArgumentException("異なる同期履歴IDの詳細を追加することはできません");
        }
        
        if (this.details == null) {
            this.details = new ArrayList<>();
        }
        
        this.details.add(detail);
    }
    
    public void addDetail(String operation, DetailStatus status, String result) {
        // details.size() + 1 でseqを自動計算
        int nextSeq = (this.details != null ? this.details.size() : 0) + 1;

        if (status == DetailStatus.SUCCESS) {
            addDetail(JiraSyncHistoryDetail.createSuccess(this.id, nextSeq, operation, result));
        } else if (status == DetailStatus.ERROR) {
            addDetail(JiraSyncHistoryDetail.createError(this.id, nextSeq, operation, result));
        }
    }

    public void addDetail(String operation, DetailStatus status, Object result) {
        String resultJson = result != null ? result.toString() : null;
        addDetail(operation, status, resultJson);
    }
    
    /**
     * 同期が実行中かを判定
     * 
     * @return 実行中の場合true
     */
    public boolean isInProgress() {
        return syncStatus.isInProgress();
    }
    
    /**
     * 同期実行時間を計算（分単位）
     * 
     * @return 実行時間（分）。実行中の場合は現在時刻までの時間、未完了の場合は0
     */
    public long getDurationMinutes() {
        if (startedAt == null) {
            return 0;
        }
        
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return Duration.between(startedAt, endTime).toMinutes();
    }
    
    /**
     * 成功率を計算
     * 
     * @return 成功率（百分率、0-100）。処理数が0の場合は100を返す
     */
    public double getSuccessRate() {
        if (totalProjectsProcessed == null || totalProjectsProcessed == 0) {
            return 100.0;
        }
        
        return (double) successCount / totalProjectsProcessed * 100.0;
    }
    
    /**
     * 詳細履歴のコレクションを取得
     * 
     * @return 詳細履歴の読み取り専用リスト
     */
    public List<JiraSyncHistoryDetail> getDetails() {
        return details != null ? Collections.unmodifiableList(details) : Collections.emptyList();
    }
    
    /**
     * 詳細履歴を設定（リポジトリからの読み込み用）
     * 
     * @param details 詳細履歴のリスト
     */
    public void setDetails(List<JiraSyncHistoryDetail> details) {
        this.details = details != null ? new ArrayList<>(details) : new ArrayList<>();
    }
    
    // ゲッター
    public String getId() { return id; }
    public JiraSyncType getSyncType() { return syncType; }
    public JiraSyncStatus getSyncStatus() { return syncStatus; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Integer getTotalProjectsProcessed() { return totalProjectsProcessed; }
    public Integer getSuccessCount() { return successCount; }
    public Integer getErrorCount() { return errorCount; }
    public String getErrorDetails() { return errorDetails; }
    public String getTriggeredBy() { return triggeredBy; }
    
    // MyBatis用のpackage-privateセッター
    void setId(String id) { this.id = id; }
    void setSyncType(JiraSyncType syncType) { this.syncType = syncType; }
    void setSyncStatus(JiraSyncStatus syncStatus) { this.syncStatus = syncStatus; }
    void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    void setTotalProjectsProcessed(Integer totalProjectsProcessed) { this.totalProjectsProcessed = totalProjectsProcessed; }
    void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
    void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    
    /**
     * 等価性の判定（IDベース）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JiraSyncHistory that = (JiraSyncHistory) obj;
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
        return String.format("SyncHistory{id='%s', syncType=%s, syncStatus=%s, startedAt=%s, totalProcessed=%d, success=%d, error=%d}", 
                           id, syncType, syncStatus, startedAt, totalProjectsProcessed, successCount, errorCount);
    }
}