package com.devhour.application.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * バッチ処理結果DTO
 * 
 * 承認権限者CSVバッチ処理の実行結果を表現するデータ転送オブジェクト
 * 処理統計、エラー情報、承認者関係の変更情報を含む
 * 
 * 責務:
 * - バッチ処理の統計情報管理（処理件数、追加・更新・削除件数）
 * - エラー情報の収集・管理
 * - 承認者関係の変更統計
 * - 処理結果のサマリー提供
 */
public class BatchResult {
    
    /**
     * 処理済み件数（成功した承認権限者数）
     */
    private int processed = 0;
    
    /**
     * 新規追加件数
     */
    private int added = 0;
    
    /**
     * 更新件数
     */
    private int updated = 0;
    
    /**
     * 削除件数
     */
    private int deleted = 0;
    
    /**
     * エラーメッセージリスト
     */
    private List<String> errors = new ArrayList<>();
    
    /**
     * 承認者関係の変更結果
     */
    private ApproverRelationResult approverRelations = new ApproverRelationResult();
    
    /**
     * 処理開始時刻
     */
    private LocalDateTime startTime = LocalDateTime.now();
    
    /**
     * 処理終了時刻
     */
    private LocalDateTime endTime;
    
    /**
     * 承認者関係の変更結果
     * 承認者関係テーブルの追加・削除統計を管理
     */
    public static class ApproverRelationResult {
        /**
         * 追加された承認者関係数
         */
        private int added = 0;
        
        /**
         * 削除された承認者関係数
         */
        private int deleted = 0;
        
        // getters and setters
        public int getAdded() { return added; }
        public void setAdded(int added) { this.added = added; }
        
        public int getDeleted() { return deleted; }
        public void setDeleted(int deleted) { this.deleted = deleted; }
        
        /**
         * 承認者関係の追加カウントをインクリメント
         */
        public void incrementAdded() {
            this.added++;
        }
        
        /**
         * 承認者関係の削除カウントをインクリメント
         */
        public void incrementDeleted() {
            this.deleted++;
        }
        
        /**
         * 承認者関係に変更があったかチェック
         * @return 追加または削除があった場合true
         */
        public boolean hasChanges() {
            return added > 0 || deleted > 0;
        }
        
        /**
         * 変更の総数を取得
         * @return 追加と削除の合計数
         */
        public int getTotalChanges() {
            return added + deleted;
        }
        
        @Override
        public String toString() {
            return String.format("ApproverRelationResult{added=%d, deleted=%d}", added, deleted);
        }
    }
    
    // Basic getters and setters
    public int getProcessed() { return processed; }
    public void setProcessed(int processed) { this.processed = processed; }
    
    public int getAdded() { return added; }
    public void setAdded(int added) { this.added = added; }
    
    public int getUpdated() { return updated; }
    public void setUpdated(int updated) { this.updated = updated; }
    
    public int getDeleted() { return deleted; }
    public void setDeleted(int deleted) { this.deleted = deleted; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    
    public ApproverRelationResult getApproverRelations() { return approverRelations; }
    public void setApproverRelations(ApproverRelationResult approverRelations) { this.approverRelations = approverRelations; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    // Convenience methods for incrementing counters
    /**
     * 処理済み件数をインクリメント
     */
    public void incrementProcessed() {
        this.processed++;
    }
    
    /**
     * 追加件数をインクリメント
     */
    public void incrementAdded() {
        this.added++;
    }
    
    /**
     * 更新件数をインクリメント
     */
    public void incrementUpdated() {
        this.updated++;
    }
    
    /**
     * 削除件数をインクリメント
     */
    public void incrementDeleted() {
        this.deleted++;
    }
    
    /**
     * エラーメッセージを追加
     * @param errorMessage エラーメッセージ
     */
    public void addError(String errorMessage) {
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            this.errors.add(errorMessage.trim());
        }
    }
    
    /**
     * 複数のエラーメッセージを追加
     * @param errorMessages エラーメッセージのリスト
     */
    public void addErrors(List<String> errorMessages) {
        if (errorMessages != null) {
            errorMessages.forEach(this::addError);
        }
    }
    
    /**
     * 承認者関係の追加件数をインクリメント
     */
    public void incrementApproverAdded() {
        this.approverRelations.incrementAdded();
    }
    
    /**
     * 承認者関係の削除件数をインクリメント
     */
    public void incrementApproverDeleted() {
        this.approverRelations.incrementDeleted();
    }
    
    /**
     * 処理完了を記録
     */
    public void markCompleted() {
        this.endTime = LocalDateTime.now();
    }
    
    /**
     * 処理時間を取得（ミリ秒）
     * @return 処理時間（処理完了前の場合は現在時刻からの経過時間）
     */
    public long getProcessingTimeMillis() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMillis();
    }
    
    /**
     * 処理が成功したかチェック
     * @return エラーがない場合true
     */
    public boolean isSuccessful() {
        return errors.isEmpty();
    }
    
    /**
     * 部分的に成功したかチェック
     * @return 一部処理されたがエラーもある場合true
     */
    public boolean isPartiallySuccessful() {
        return processed > 0 && !errors.isEmpty();
    }
    
    /**
     * 完全に失敗したかチェック
     * @return 何も処理されずエラーがある場合true
     */
    public boolean isCompleteFailure() {
        return processed == 0 && !errors.isEmpty();
    }
    
    /**
     * 変更があったかチェック
     * @return 追加、更新、削除、承認者関係変更のいずれかがあった場合true
     */
    public boolean hasChanges() {
        return added > 0 || updated > 0 || deleted > 0 || approverRelations.hasChanges();
    }
    
    /**
     * 統計情報の総括を取得
     * @return 処理統計のサマリー文字列
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("BatchResult Summary: Processed=%d, Added=%d, Updated=%d, Deleted=%d", 
                      processed, added, updated, deleted));
        
        if (approverRelations.hasChanges()) {
            summary.append(String.format(", ApproverRelations(Added=%d, Deleted=%d)", 
                          approverRelations.getAdded(), approverRelations.getDeleted()));
        }
        
        if (!errors.isEmpty()) {
            summary.append(String.format(", Errors=%d", errors.size()));
        }
        
        if (endTime != null) {
            summary.append(String.format(", ProcessingTime=%dms", getProcessingTimeMillis()));
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}