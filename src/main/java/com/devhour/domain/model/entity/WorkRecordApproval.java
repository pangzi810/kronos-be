package com.devhour.domain.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.devhour.domain.model.valueobject.ApprovalStatus;

import lombok.Getter;

/**
 * 作業記録承認エンティティ
 * 申請者/日付単位で承認状態を管理する
 */
@Getter
public class WorkRecordApproval {
    
    /** 申請者ID */
    private final String userId;
    
    /** 作業日 */
    private final LocalDate workDate;
    
    /** 承認ステータス */
    private ApprovalStatus approvalStatus;
    
    /** 承認者ID */
    private String approverId;
    
    /** 承認日時 */
    private LocalDateTime approvedAt;
    
    /** 却下理由 */
    private String rejectionReason;
    
    /** 作成日時 */
    private LocalDateTime createdAt;
    
    /** 更新日時 */
    private LocalDateTime updatedAt;

    /**
     * 新規作成用コンストラクタ
     * 
     * @param userId 申請者ID
     * @param workDate 作業日
     */
    public WorkRecordApproval(String userId, LocalDate workDate) {
        validateUserId(userId);
        validateWorkDate(workDate);
        
        this.userId = userId;
        this.workDate = workDate;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 既存データ復元用コンストラクタ
     * 
     * @param userId 申請者ID
     * @param workDate 作業日
     * @param approvalStatus 承認ステータス
     * @param approverId 承認者ID
     * @param approvedAt 承認日時
     * @param rejectionReason 却下理由
     */
    public WorkRecordApproval(
            String userId,
            LocalDate workDate,
            ApprovalStatus approvalStatus,
            String approverId,
            LocalDateTime approvedAt,
            String rejectionReason) {
        
        validateUserId(userId);
        validateWorkDate(workDate);
        
        this.userId = userId;
        this.workDate = workDate;
        this.approvalStatus = approvalStatus != null ? approvalStatus : ApprovalStatus.PENDING;
        this.approverId = approverId;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完全なデータ復元用コンストラクタ
     */
    public WorkRecordApproval(
            String userId,
            LocalDate workDate,
            ApprovalStatus approvalStatus,
            String approverId,
            LocalDateTime approvedAt,
            String rejectionReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        
        this.userId = userId;
        this.workDate = workDate;
        this.approvalStatus = approvalStatus;
        this.approverId = approverId;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 承認する
     * 
     * @param approverId 承認者ID
     * @throws IllegalArgumentException 承認者IDがnullまたは空の場合
     * @throws IllegalStateException 既に承認済みの場合
     */
    public void approve(String approverId) {
        if (approverId == null || approverId.trim().isEmpty()) {
            throw new IllegalArgumentException("承認者IDは必須です");
        }
        
        if (isApproved()) {
            throw new IllegalStateException("既に承認済みです");
        }
        
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approverId = approverId;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = null; // 承認時は却下理由をクリア
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 差し戻す
     * 
     * @param approverId 承認者ID
     * @param reason 却下理由
     * @throws IllegalArgumentException 承認者IDまたは却下理由がnullまたは空の場合
     * @throws IllegalStateException 承認済みの場合
     */
    public void reject(String approverId, String reason) {
        if (approverId == null || approverId.trim().isEmpty()) {
            throw new IllegalArgumentException("承認者IDは必須です");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("却下理由は必須です");
        }
        
        if (isApproved()) {
            throw new IllegalStateException("承認済みのため差し戻しできません");
        }
        
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approverId = approverId;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 承認待ち状態に戻す
     * 差し戻しや承認後の状態を承認待ちに戻す
     */
    public void makePending() {
        this.approvalStatus = ApprovalStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
        this.rejectionReason = null; // 差し戻し理由をクリア
    }

    /**
     * 編集可能かチェック
     * 
     * @return 編集可能な場合true
     */
    public boolean isEditable() {
        return !isApproved();
    }

    /**
     * 承認済みかチェック
     * 
     * @return 承認済みの場合true
     */
    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.APPROVED;
    }

    /**
     * 差し戻し状態かチェック
     * 
     * @return 差し戻し状態の場合true
     */
    public boolean isRejected() {
        return approvalStatus == ApprovalStatus.REJECTED;
    }

    /**
     * 承認待ち状態かチェック
     * 
     * @return 承認待ち状態の場合true
     */
    public boolean isPending() {
        return approvalStatus == ApprovalStatus.PENDING;
    }

    /**
     * 承認可能かチェック
     * 承認待ち状態または差し戻し状態の場合に承認可能
     * 
     * @return 承認可能な場合true
     */
    public boolean canApprove() {
        return approvalStatus == ApprovalStatus.PENDING || approvalStatus == ApprovalStatus.REJECTED;
    }

    /**
     * ユーザーIDの妥当性チェック
     */
    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("申請者IDは必須です");
        }
    }

    /**
     * 作業日の妥当性チェック
     */
    private void validateWorkDate(LocalDate workDate) {
        if (workDate == null) {
            throw new IllegalArgumentException("作業日は必須です");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkRecordApproval that = (WorkRecordApproval) o;
        return Objects.equals(userId, that.userId) && 
               Objects.equals(workDate, that.workDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, workDate);
    }

    @Override
    public String toString() {
        return "WorkRecordApproval{" +
                "userId='" + userId + '\'' +
                ", workDate=" + workDate +
                ", approvalStatus=" + approvalStatus +
                ", approverId='" + approverId + '\'' +
                ", approvedAt=" + approvedAt +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }

}