package com.devhour.domain.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 承認履歴エンティティ
 * 
 * 工数記録の承認履歴を管理するエンティティ
 * イベントソーシングパターンで全ての承認アクションを記録
 */
public class WorkRecordApprovalHistory {
    
    private String historyId;
    private String workRecordId;
    private String userId;
    private String projectId;
    private LocalDate workDate;
    private String categoryHours;
    private BigDecimal totalHours;
    private String description;
    private String action;
    private String previousStatus;
    private String currentStatus;
    private String approverId;
    private String rejectionReason;
    private String workRecordSnapshot;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    
    private WorkRecordApprovalHistory() {
        // MyBatisのマッピング用
    }
    
    /**
     * プライベートコンストラクタ
     */
    private WorkRecordApprovalHistory(String historyId, String workRecordId, String userId,
                           String projectId, LocalDate workDate, String categoryHours,
                           BigDecimal totalHours, String description, String action,
                           String previousStatus, String currentStatus, String approverId,
                           String rejectionReason, String workRecordSnapshot,
                           LocalDateTime occurredAt) {
        this.historyId = historyId;
        this.workRecordId = workRecordId;
        this.userId = userId;
        this.projectId = projectId;
        this.workDate = workDate;
        this.categoryHours = categoryHours;
        this.totalHours = totalHours;
        this.description = description;
        this.action = action;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.approverId = approverId;
        this.rejectionReason = rejectionReason;
        this.workRecordSnapshot = workRecordSnapshot;
        this.occurredAt = occurredAt;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 承認アクション履歴を作成
     * 
     * @param eventId イベントID（workRecordId#version形式）
     * @param workRecord 工数記録
     * @param action アクション種別（APPROVE/REJECT/RESUBMIT）
     * @param previousStatus 変更前ステータス
     * @param approverId 承認者ID
     * @param rejectionReason 却下理由（却下の場合のみ）
     * @param workRecordSnapshot 工数記録のスナップショット（JSON）
     * @return 新しいApprovalHistoryエンティティ
     */
    public static WorkRecordApprovalHistory createFromWorkRecord(String eventId, WorkRecord workRecord,
                                                      String action, String previousStatus,
                                                      String approverId, String rejectionReason,
                                                      String workRecordSnapshot) {
        // カテゴリ別工数の合計を計算
        BigDecimal totalHours = workRecord.getCategoryHours() != null ?
            workRecord.getCategoryHours().getTotalHours() : BigDecimal.ZERO;
        
        // カテゴリ別工数をJSON文字列として保存
        String categoryHoursJson = workRecord.getCategoryHours() != null ?
            workRecord.getCategoryHours().toJsonString() : "{}";
        
        return new WorkRecordApprovalHistory(
            eventId,
            workRecord.getId(),
            workRecord.getUserId(),
            workRecord.getProjectId(),
            workRecord.getWorkDate(),
            categoryHoursJson,
            totalHours,
            workRecord.getDescription(),
            action,
            previousStatus,
            "PENDING", // 工数記録レベルでは承認状態は管理されない
            approverId,
            rejectionReason,
            workRecordSnapshot,
            LocalDateTime.now()
        );
    }
    
    /**
     * 既存の承認履歴を復元
     */
    public static WorkRecordApprovalHistory restore(String historyId, String workRecordId, String userId,
                                         String projectId, LocalDate workDate, String categoryHours,
                                         BigDecimal totalHours, String description, String action,
                                         String previousStatus, String currentStatus, String approverId,
                                         String rejectionReason, String workRecordSnapshot,
                                         LocalDateTime occurredAt, LocalDateTime createdAt) {
        WorkRecordApprovalHistory history = new WorkRecordApprovalHistory();
        history.historyId = historyId;
        history.workRecordId = workRecordId;
        history.userId = userId;
        history.projectId = projectId;
        history.workDate = workDate;
        history.categoryHours = categoryHours;
        history.totalHours = totalHours;
        history.description = description;
        history.action = action;
        history.previousStatus = previousStatus;
        history.currentStatus = currentStatus;
        history.approverId = approverId;
        history.rejectionReason = rejectionReason;
        history.workRecordSnapshot = workRecordSnapshot;
        history.occurredAt = occurredAt;
        history.createdAt = createdAt;
        return history;
    }
    
    /**
     * 承認アクションかチェック
     * 
     * @return 承認アクションの場合true
     */
    public boolean isApprovalAction() {
        return "APPROVE".equals(action);
    }
    
    /**
     * 却下アクションかチェック
     * 
     * @return 却下アクションの場合true
     */
    public boolean isRejectionAction() {
        return "REJECT".equals(action);
    }
    
    /**
     * 再申請アクションかチェック
     * 
     * @return 再申請アクションの場合true
     */
    public boolean isResubmitAction() {
        return "RESUBMIT".equals(action);
    }
    
    // ゲッター
    public String getHistoryId() { return historyId; }
    public String getWorkRecordId() { return workRecordId; }
    public String getUserId() { return userId; }
    public String getProjectId() { return projectId; }
    public LocalDate getWorkDate() { return workDate; }
    public String getCategoryHours() { return categoryHours; }
    public BigDecimal getTotalHours() { return totalHours; }
    public String getDescription() { return description; }
    public String getAction() { return action; }
    public String getPreviousStatus() { return previousStatus; }
    public String getCurrentStatus() { return currentStatus; }
    public String getApproverId() { return approverId; }
    public String getRejectionReason() { return rejectionReason; }
    public String getWorkRecordSnapshot() { return workRecordSnapshot; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WorkRecordApprovalHistory that = (WorkRecordApprovalHistory) obj;
        return Objects.equals(historyId, that.historyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(historyId);
    }
    
    @Override
    public String toString() {
        return String.format("ApprovalHistory{historyId='%s', workRecordId='%s', action='%s', currentStatus='%s', occurredAt=%s}",
            historyId, workRecordId, action, currentStatus, occurredAt);
    }
}